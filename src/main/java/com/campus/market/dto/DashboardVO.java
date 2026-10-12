package com.campus.market.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 后台数据看板统计结果
 */
@Data
public class DashboardVO {

    /** 用户总数 */
    private Long userCount;

    /** 商品总数 */
    private Long productCount;

    /** 在售商品数 */
    private Long onSaleCount;

    /** 已售商品数 */
    private Long soldCount;

    /** 订单总数 */
    private Long orderCount;

    /** 已完成订单数 */
    private Long finishedCount;

    /** 平台累计成交金额（已完成订单） */
    private BigDecimal totalGmv;

    /** 近 7 日每日订单量：[{day, count}, ...] */
    private List<Map<String, Object>> trend;

    /** 分类商品分布（前 6）：[{name, count}, ...] */
    private List<Map<String, Object>> categories;
}
