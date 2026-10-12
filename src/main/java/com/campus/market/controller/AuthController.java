package com.campus.market.controller;

import com.campus.market.annotation.RequireLogin;
import com.campus.market.common.Result;
import com.campus.market.dto.LoginDTO;
import com.campus.market.dto.LoginVO;
import com.campus.market.dto.RegisterDTO;
import com.campus.market.entity.User;
import com.campus.market.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 注册 / 登录
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<Long> register(@Valid @RequestBody RegisterDTO dto) {
        return Result.success("注册成功", userService.register(dto));
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success("登录成功", userService.login(dto));
    }

    @GetMapping("/me")
    @RequireLogin
    public Result<User> me() {
        return Result.success(userService.getCurrentUser());
    }
}
