package com.campus.market.service;

import com.campus.market.common.PageResult;
import com.campus.market.dto.OrderCreateDTO;
import com.campus.market.dto.OrderQuery;
import com.campus.market.entity.Orders;

public interface OrderService {

    /** 下单：生成待支付订单 */
    Long create(OrderCreateDTO dto);

    /** 模拟支付：支付成功后占用商品 */
    void pay(Long orderId);

    /** 我的订单列表 */
    PageResult<Orders> page(OrderQuery query);

    /** 卖家确认交易 */
    void confirm(Long orderId);

    /** 买家确认完成 */
    void finish(Long orderId);

    /** 取消订单 */
    void cancel(Long orderId);
}
