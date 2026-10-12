package com.campus.market.service;

import com.campus.market.common.PageResult;
import com.campus.market.dto.LoginDTO;
import com.campus.market.dto.LoginVO;
import com.campus.market.dto.RegisterDTO;
import com.campus.market.dto.UserUpdateDTO;
import com.campus.market.entity.User;

public interface UserService {

    /** 注册 */
    Long register(RegisterDTO dto);

    /** 登录，返回 token 与用户信息 */
    LoginVO login(LoginDTO dto);

    /** 当前登录用户 */
    User getCurrentUser();

    User getById(Long id);

    void updateProfile(UserUpdateDTO dto);

    void changePassword(String oldPassword, String newPassword);

    // ---------- 管理端 ----------

    PageResult<User> pageUsers(Integer pageNum, Integer pageSize, String keyword);

    void updateStatus(Long userId, Integer status);
}
