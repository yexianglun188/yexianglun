package com.campus.market.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 下单参数
 */
@Data
public class OrderCreateDTO {

    @NotNull(message = "请选择商品")
    private Long productId;

    @NotBlank(message = "请填写联系方式")
    @Size(max = 50, message = "联系方式过长")
    private String contact;

    @Size(max = 255, message = "留言不能超过255个字")
    private String remark;
}
