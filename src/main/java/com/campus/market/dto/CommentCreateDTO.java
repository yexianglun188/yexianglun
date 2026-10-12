package com.campus.market.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 留言参数
 */
@Data
public class CommentCreateDTO {

    @NotNull(message = "参数错误")
    private Long productId;

    @NotBlank(message = "留言内容不能为空")
    @Size(max = 500, message = "留言不能超过500个字")
    private String content;

    private Long parentId;
}
