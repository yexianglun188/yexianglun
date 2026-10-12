package com.campus.market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.market.dto.DashboardVO;
import com.campus.market.entity.Orders;
import com.campus.market.entity.Product;
import com.campus.market.entity.User;
import com.campus.market.mapper.OrdersMapper;
import com.campus.market.mapper.ProductMapper;
import com.campus.market.mapper.UserMapper;
import com.campus.market.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final OrdersMapper ordersMapper;

    @Override
    public DashboardVO stats() {
        DashboardVO vo = new DashboardVO();
        vo.setUserCount(userMapper.selectCount(new QueryWrapper<User>().eq("deleted", 0)));
        vo.setProductCount(productMapper.selectCount(new QueryWrapper<Product>().eq("deleted", 0)));
        vo.setOnSaleCount(productMapper.selectCount(
                new QueryWrapper<Product>().eq("deleted", 0).eq("status", Product.STATUS_ON_SALE)));
        vo.setSoldCount(productMapper.selectCount(
                new QueryWrapper<Product>().eq("deleted", 0).eq("status", Product.STATUS_SOLD)));
        vo.setOrderCount(ordersMapper.selectCount(new QueryWrapper<Orders>().eq("deleted", 0)));
        vo.setFinishedCount(ordersMapper.selectCount(
                new QueryWrapper<Orders>().eq("deleted", 0).eq("status", Orders.STATUS_FINISHED)));
        vo.setTotalGmv(ordersMapper.sumFinishedAmount());
        vo.setTrend(ordersMapper.countByDayLast7());
        vo.setCategories(productMapper.countByCategory());
        return vo;
    }
}
