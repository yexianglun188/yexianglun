package com.campus.market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.market.dto.OrderQuery;
import com.campus.market.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {

    /** 分页查询订单（联表带出商品、买卖双方信息） */
    IPage<Orders> selectOrderPage(IPage<Orders> page, @Param("query") OrderQuery query);

    /** 已完成订单(status=3)的成交总额 */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM orders WHERE deleted = 0 AND status = 3")
    BigDecimal sumFinishedAmount();

    /** 近 7 日每日订单量 */
    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m-%d') AS day, COUNT(*) AS count " +
            "FROM orders WHERE deleted = 0 AND create_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
            "GROUP BY DATE_FORMAT(create_time, '%Y-%m-%d') ORDER BY day")
    List<Map<String, Object>> countByDayLast7();
}
