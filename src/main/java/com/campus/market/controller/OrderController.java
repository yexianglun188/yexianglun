package com.campus.market.controller;

import com.campus.market.annotation.RequireLogin;
import com.campus.market.common.PageResult;
import com.campus.market.common.Result;
import com.campus.market.dto.OrderCreateDTO;
import com.campus.market.dto.OrderQuery;
import com.campus.market.entity.Orders;
import com.campus.market.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /** 下单（生成待支付订单，暂不占用商品） */
    @PostMapping
    @RequireLogin
    public Result<Long> create(@Valid @RequestBody OrderCreateDTO dto) {
        return Result.success("下单成功，请尽快完成支付", orderService.create(dto));
    }

    /** 支付订单（模拟支付，仅买家可操作） */
    @PutMapping("/{id}/pay")
    @RequireLogin
    public Result<Void> pay(@PathVariable Long id) {
        orderService.pay(id);
        return Result.success("支付成功", null);
    }

    /** 我的订单（role=buy 我买到的 / role=sell 我卖出的） */
    @GetMapping
    @RequireLogin
    public Result<PageResult<Orders>> list(OrderQuery query) {
        return Result.success(orderService.page(query));
    }

    /** 卖家确认交易 */
    @PutMapping("/{id}/confirm")
    @RequireLogin
    public Result<Void> confirm(@PathVariable Long id) {
        orderService.confirm(id);
        return Result.success("已确认交易", null);
    }

    /** 买家确认完成 */
    @PutMapping("/{id}/finish")
    @RequireLogin
    public Result<Void> finish(@PathVariable Long id) {
        orderService.finish(id);
        return Result.success("交易完成", null);
    }

    /** 取消订单 */
    @PutMapping("/{id}/cancel")
    @RequireLogin
    public Result<Void> cancel(@PathVariable Long id) {
        orderService.cancel(id);
        return Result.success("订单已取消", null);
    }
}
