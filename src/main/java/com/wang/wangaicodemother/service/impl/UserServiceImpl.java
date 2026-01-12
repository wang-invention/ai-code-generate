package com.wang.wangaicodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.wang.wangaicodemother.enums.UserRoleEnum;
import com.wang.wangaicodemother.exception.BusinessException;
import com.wang.wangaicodemother.exception.ErrorCode;
import com.wang.wangaicodemother.model.entity.User;
import com.wang.wangaicodemother.mapper.UserMapper;
import com.wang.wangaicodemother.model.vo.LoginUserVO;
import com.wang.wangaicodemother.service.UserService;
import com.wang.wangaicodemother.utils.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;


/**
 * 用户 服务层实现。
 *
 * @author <a href="https://github.com">wangInvention</a>
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


    @Autowired
    private UserMapper userMapper;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验参数
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账户小于4位");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码小于8位");
        }
        // 2. 查询用户是否存在
        long userAccount1 = userMapper.selectCountByQuery(new QueryWrapper().eq("userAccount", userAccount));
        if (userAccount1 > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已存在");
        }
        // 3. 加密密码
        String encryptedPassword = getEncryptedPassword(userPassword);
        // 4, 插入数据库
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptedPassword);
        user.setUserRole(UserRoleEnum.USER.getValue());
        user.setUserName("无名");
        boolean save = this.save(user);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败");
        }
        return user.getId();
    }


    /**
     * 获取加密密码
     *
     * @param userPassword
     * @return
     */
    public String getEncryptedPassword(String userPassword) {
        // 密码加密
        final String slat = "wangInvention";
        return DigestUtils.md5DigestAsHex((userPassword + slat).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 用户登录
     *
     * @param userAccount
     * @param userPassword
     * @return
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword) {
        // 1. 校验参数
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 2. 校验密码
        String encryptedPassword = getEncryptedPassword(userPassword);
        QueryWrapper query = new QueryWrapper().eq("userAccount", userAccount);
        User user = userMapper.selectOneByQuery(query);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        if (!user.getUserPassword().equals(encryptedPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        loginUserVO.setToken(JwtUtil.generateToken(user.getId(), user.getUserRole()));
        return loginUserVO;
    }
}
