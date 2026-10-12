package com.campus.market.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品发布 / 编辑参数
 */
@Data
public class ProductFormDTO {

    /** 编辑时携带 */
    private Long id;

    @NotNull(message = "请选择商品分类")
    private Long categoryId;

    @NotBlank(message = "请输入商品标题")
    @Size(max = 100, message = "标题不能超过100个字")
    private String title;

    @Size(max = 2000, message = "描述不能超过2000个字")
    private String description;

    @NotNull(message = "请输入售价")
    @DecimalMin(value = "0.01", message = "售价必须大于0")
    private BigDecimal price;

    private BigDecimal originalPrice;

    private String coverImage;

    private List<String> images;

    private String itemCondition;

    private String tradePlace;
}
