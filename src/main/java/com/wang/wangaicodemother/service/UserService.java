package com.wang.wangaicodemother.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.wang.wangaicodemother.model.dto.UserQueryRequest;
import com.wang.wangaicodemother.model.entity.User;
import com.wang.wangaicodemother.model.vo.LoginUserVO;
import com.wang.wangaicodemother.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 用户 服务层。
 *
 * @author <a href="https://github.com">wangInvention</a>
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);
    /**
     * 获取加密密码
     *
     * @param userPassword  用户密码
     * @return 加密后的密码
     */
    public String getEncryptedPassword(String userPassword) ;

    /**
     * 登录
     */
    LoginUserVO userLogin(String userAccount, String userPassword);

    /**
     * 获取当前登录用户
     */
    User getLoginUser(HttpServletRequest  request);

    LoginUserVO getLoginUserVO(User loginUser);

    /**
     * 退出登录
     * @param request
     * @return
     */
    String userLogout(HttpServletRequest request);


    /**
     * 获取脱敏的用户
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏的用户列表
     * @param userList
     * @return
     */
    List<UserVO> getUserVOList(List<User> userList);


    /**
     * 获取查询条件
     * @param userQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);
}
