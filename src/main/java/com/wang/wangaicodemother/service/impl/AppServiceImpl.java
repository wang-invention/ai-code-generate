package com.wang.wangaicodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.wang.wangaicodemother.constants.AppConstant;
import com.wang.wangaicodemother.core.AiCodeGeneratorFacade;
import com.wang.wangaicodemother.core.builder.VueProjectBuilder;
import com.wang.wangaicodemother.core.handler.StreamHandlerExecutor;
import com.wang.wangaicodemother.enums.CodeGenTypeEnum;
import com.wang.wangaicodemother.exception.BusinessException;
import com.wang.wangaicodemother.exception.ErrorCode;
import com.wang.wangaicodemother.exception.ThrowUtils;
import com.wang.wangaicodemother.mapper.AppMapper;
import com.wang.wangaicodemother.model.dto.AppQueryRequest;
import com.wang.wangaicodemother.model.entity.App;
import com.wang.wangaicodemother.model.entity.User;
import com.wang.wangaicodemother.model.vo.AppVO;
import com.wang.wangaicodemother.model.vo.UserVO;
import com.wang.wangaicodemother.service.AppService;
import com.wang.wangaicodemother.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 * @author <a href="https://github.com">wangInvention</a>
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {


    @Resource
    private UserServiceImpl userService;


    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Resource
    private StreamHandlerExecutor streamHandlerExecutor;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private VueProjectBuilder vueProjectBuilder;


    /**
     * 获取App信息和关联的用户信息
     *
     * @param app
     * @return
     */
    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        // 关联查询用户信息
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    /**
     * 将app列表转换为VO列表
     *
     * @param appList
     * @return
     */
    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        //批量获取用户信息
        Set<Long> userIdList = appList.stream().map(App::getUserId).collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIdList).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            appVO.setUser(userVOMap.get(app.getUserId()));
            return appVO;
        }).collect(Collectors.toList());
    }

    @Override
    public Flux<String> chatToGenCode(String userMessage, String appId, User loginUser) {
        if (appId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用id不能为空");
        }
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户信息不能为空");
        }
        //查询应用信息
        App app = this.getById(Long.parseLong(appId));
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        if (!loginUser.getId().equals(app.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "用户没有权限,只能使用自己创建的应用");
        }

        String codeType = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeType);
        Flux<String> streamFlux = aiCodeGeneratorFacade.generateAndSaveCodeStream(userMessage, codeGenTypeEnum, appId);
        return streamHandlerExecutor.doExecute(streamFlux, chatHistoryService, Long.parseLong(appId), loginUser, codeGenTypeEnum);
    }

    @Override
    public String deployApp(Long appId, User loginUser) {
        //校验参数
        if (appId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用id不能为空");
        }
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户信息不能为空");
        }
        //检查app是否存在
        App app = this.getById(appId);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        }
        //只能部署本人的应用
        if (!loginUser.getId().equals(app.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "用户没有权限,只能部署自己创建的应用");
        }
        //将生成的app复制到部署目录下面
        String deployKey = app.getDeployKey();
        if (StrUtil.isBlank(deployKey)) {
            // 生成6位字母数字随机字符串作为部署键
            deployKey = RandomUtil.randomStringUpper(6);
        }
        // 验证部署键格式（只允许字母和数字）
        if (!deployKey.matches("^[A-Za-z0-9]+$")) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署键格式不正确");
        }
        //获取代码生成类型，构建目录
        String codeType = app.getCodeGenType();
        String sourceDirName = codeType + "_" + appId;
        String sourceDir = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        //检查代码是否存在
        File file = new File(sourceDir);
        if (!file.exists() || !file.isDirectory()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "代码不存在");
        }

        // 7. Vue 项目特殊处理：执行构建
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeType);
        if (codeGenTypeEnum == CodeGenTypeEnum.VUE_PROJECT) {
            // Vue 项目需要构建
            boolean buildSuccess = vueProjectBuilder.buildProject(sourceDir);
            ThrowUtils.throwIf(!buildSuccess, ErrorCode.SYSTEM_ERROR, "Vue 项目构建失败，请检查代码和依赖");
            // 检查 dist 目录是否存在
            File distDir = new File(sourceDir, "dist");
            ThrowUtils.throwIf(!distDir.exists(), ErrorCode.SYSTEM_ERROR, "Vue 项目构建完成但未生成 dist 目录");
            // 将 dist 目录作为部署源
            file = distDir;
            log.info("Vue 项目构建成功，将部署 dist 目录: {}", distDir.getAbsolutePath());
        }


        //复制文件到部署目录
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(file, new File(deployDirPath), true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码复制失败");
        }
        //更新app部署状态
        App update = new App();
        update.setId(appId);
        update.setDeployKey(deployKey);
        update.setDeployedTime(LocalDateTime.now());
        boolean result = this.updateById(update);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新应用状态失败");
        }
        //返回部署目录
        return String.format("%s/%s", AppConstant.CODE_DEPLOY_HOST, deployKey);
    }


}
