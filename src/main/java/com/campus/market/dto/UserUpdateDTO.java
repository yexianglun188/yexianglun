package com.campus.market.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改个人资料参数
 */
@Data
public class UserUpdateDTO {

    @Size(max = 20, message = "昵称不能超过20个字")
    private String nickname;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private String campus;

    private String avatar;
}
