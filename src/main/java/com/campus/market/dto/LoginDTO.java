package com.campus.market.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录参数
 */
@Data
public class LoginDTO {

    @NotBlank(message = "请输入用户名")
    private String username;

    @NotBlank(message = "请输入密码")
    private String password;
}
