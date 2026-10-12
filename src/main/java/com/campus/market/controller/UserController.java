package com.campus.market.controller;

import com.campus.market.annotation.RequireLogin;
import com.campus.market.common.Result;
import com.campus.market.dto.UserUpdateDTO;
import com.campus.market.entity.User;
import com.campus.market.service.UserService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 个人中心
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 查看他人主页基本信息 */
    @GetMapping("/{id}")
    public Result<User> detail(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    /** 修改个人资料 */
    @PutMapping("/profile")
    @RequireLogin
    public Result<Void> updateProfile(@Valid @RequestBody UserUpdateDTO dto) {
        userService.updateProfile(dto);
        return Result.success("保存成功", null);
    }

    /** 修改密码 */
    @PutMapping("/password")
    @RequireLogin
    public Result<Void> changePassword(@RequestBody PasswordDTO dto) {
        userService.changePassword(dto.getOldPassword(), dto.getNewPassword());
        return Result.success("密码修改成功", null);
    }

    @Data
    public static class PasswordDTO {
        private String oldPassword;
        private String newPassword;
    }
}
