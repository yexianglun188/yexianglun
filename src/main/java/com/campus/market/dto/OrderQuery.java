package com.campus.market.dto;

import lombok.Data;

/**
 * 订单列表查询条件
 */
@Data
public class OrderQuery {

    /** 当前用户id */
    private Long userId;

    /** buy 我买到的 / sell 我卖出的 */
    private String role = "buy";

    private Integer status;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
