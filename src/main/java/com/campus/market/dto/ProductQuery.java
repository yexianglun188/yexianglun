package com.campus.market.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品列表查询条件
 */
@Data
public class ProductQuery {

    /** 关键字（标题 / 描述） */
    private String keyword;

    private Long categoryId;

    /** 商品状态 */
    private Integer status;

    /** 指定卖家 */
    private Long sellerId;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    /** 排序：latest 最新 / priceAsc 价格升 / priceDesc 价格降 / hot 最热 */
    private String sort = "latest";

    private Integer pageNum = 1;

    private Integer pageSize = 12;
}
