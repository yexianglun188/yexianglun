package com.campus.market.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.market.common.BusinessException;
import com.campus.market.common.PageResult;
import com.campus.market.common.ResultCode;
import com.campus.market.dto.OrderCreateDTO;
import com.campus.market.dto.OrderQuery;
import com.campus.market.entity.Orders;
import com.campus.market.entity.Product;
import com.campus.market.mapper.OrdersMapper;
import com.campus.market.mapper.ProductMapper;
import com.campus.market.service.OrderService;
import com.campus.market.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrdersMapper ordersMapper;
    private final ProductMapper productMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(OrderCreateDTO dto) {
        Long buyerId = UserContext.requireUserId();

        Product product = productMapper.selectById(dto.getProductId());
        if (product == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "商品不存在");
        }
        if (product.getStatus() != Product.STATUS_ON_SALE) {
            throw new BusinessException("该商品已下架或已售出");
        }
        if (product.getUserId().equals(buyerId)) {
            throw new BusinessException("不能购买自己发布的商品");
        }

        // 仅创建「待支付」订单，暂不占用商品；支付成功时再抢占，避免未付款却锁定库存
        Orders order = new Orders();
        order.setOrderNo(generateOrderNo());
        order.setProductId(product.getId());
        order.setBuyerId(buyerId);
        order.setSellerId(product.getUserId());
        order.setAmount(product.getPrice());
        order.setStatus(Orders.STATUS_UNPAID);
        order.setContact(dto.getContact());
        order.setRemark(dto.getRemark());
        ordersMapper.insert(order);
        return order.getId();
    }

    /** 模拟支付：支付成功才占用商品，从数据库层面防止同一商品被重复下单 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pay(Long orderId) {
        Orders order = requireOrder(orderId);
        if (!order.getBuyerId().equals(UserContext.requireUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "只有买家可以支付订单");
        }
        if (order.getStatus() != Orders.STATUS_UNPAID) {
            throw new BusinessException("当前订单状态不支持支付");
        }

        // 条件抢占商品：只有仍处于「在售」状态才能成功
        int updated = productMapper.update(null, new LambdaUpdateWrapper<Product>()
                .eq(Product::getId, order.getProductId())
                .eq(Product::getStatus, Product.STATUS_ON_SALE)
                .set(Product::getStatus, Product.STATUS_SOLD));
        if (updated == 0) {
            throw new BusinessException("手慢了，该商品已被他人抢先购买或已下架");
        }

        Orders paid = new Orders();
        paid.setId(orderId);
        paid.setStatus(Orders.STATUS_PENDING);
        paid.setPaidTime(LocalDateTime.now());
        ordersMapper.updateById(paid);
    }

    @Override
    public PageResult<Orders> page(OrderQuery query) {
        query.setUserId(UserContext.requireUserId());
        Page<Orders> page = new Page<>(
                query.getPageNum() == null ? 1 : query.getPageNum(),
                query.getPageSize() == null ? 10 : query.getPageSize());
        IPage<Orders> result = ordersMapper.selectOrderPage(page, query);
        return PageResult.of(result.getTotal(), result.getRecords());
    }

    /** 卖家确认：待确认 -> 交易中 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirm(Long orderId) {
        Orders order = requireOrder(orderId);
        if (!order.getSellerId().equals(UserContext.requireUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "只有卖家可以确认交易");
        }
        if (order.getStatus() != Orders.STATUS_PENDING) {
            throw new BusinessException("当前订单状态不支持该操作");
        }
        updateStatus(orderId, Orders.STATUS_TRADING);
    }

    /** 买家确认完成：交易中 -> 已完成 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finish(Long orderId) {
        Orders order = requireOrder(orderId);
        if (!order.getBuyerId().equals(UserContext.requireUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "只有买家可以确认完成");
        }
        if (order.getStatus() != Orders.STATUS_TRADING && order.getStatus() != Orders.STATUS_PENDING) {
            throw new BusinessException("当前订单状态不支持该操作");
        }
        Orders update = new Orders();
        update.setId(orderId);
        update.setStatus(Orders.STATUS_FINISHED);
        update.setFinishTime(LocalDateTime.now());
        ordersMapper.updateById(update);
    }

    /** 取消订单：商品重新上架 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long orderId) {
        Orders order = requireOrder(orderId);
        Long userId = UserContext.requireUserId();
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权操作该订单");
        }
        if (order.getStatus() == Orders.STATUS_FINISHED) {
            throw new BusinessException("已完成的订单不能取消");
        }
        if (order.getStatus() == Orders.STATUS_CANCELED) {
            throw new BusinessException("订单已取消");
        }

        updateStatus(orderId, Orders.STATUS_CANCELED);

        // 商品重新上架（仅当仍处于「已售」状态时）
        productMapper.update(null, new LambdaUpdateWrapper<Product>()
                .eq(Product::getId, order.getProductId())
                .eq(Product::getStatus, Product.STATUS_SOLD)
                .set(Product::getStatus, Product.STATUS_ON_SALE));
    }

    // ------------------------------------------------------------

    private Orders requireOrder(Long orderId) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "订单不存在");
        }
        return order;
    }

    private void updateStatus(Long orderId, int status) {
        Orders update = new Orders();
        update.setId(orderId);
        update.setStatus(status);
        ordersMapper.updateById(update);
    }

    private String generateOrderNo() {
        return DateUtil.format(new Date(), "yyyyMMddHHmmssSSS") + RandomUtil.randomNumbers(4);
    }
}
