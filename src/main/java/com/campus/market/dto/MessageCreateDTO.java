package com.campus.market.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发送私信请求
 */
@Data
public class MessageCreateDTO {

    /** 接收者id */
    @NotNull(message = "接收者不能为空")
    private Long toId;

    /** 关联商品id（可选） */
    private Long productId;

    /** 消息内容 */
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 1000, message = "消息内容不能超过 1000 字")
    private String content;
}
