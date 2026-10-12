package com.campus.market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.market.dto.ProductQuery;
import com.campus.market.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /** 分页查询商品列表（联表带出卖家与分类信息） */
    IPage<Product> selectProductPage(IPage<Product> page, @Param("query") ProductQuery query);

    /** 查询商品详情（联表带出卖家与分类信息） */
    Product selectDetailById(@Param("id") Long id);

    /** 各分类商品数量分布（前 6） */
    @Select("SELECT c.name AS name, COUNT(p.id) AS count " +
            "FROM product p LEFT JOIN category c ON p.category_id = c.id " +
            "WHERE p.deleted = 0 GROUP BY c.id, c.name ORDER BY count DESC LIMIT 6")
    List<Map<String, Object>> countByCategory();
}
