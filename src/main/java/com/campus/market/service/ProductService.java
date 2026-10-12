package com.campus.market.service;

import com.campus.market.common.PageResult;
import com.campus.market.dto.ProductFormDTO;
import com.campus.market.dto.ProductQuery;
import com.campus.market.entity.Product;

public interface ProductService {

    /** 商品列表（前台，只展示在售） */
    PageResult<Product> pageOnSale(ProductQuery query);

    /** 商品详情（自动累加浏览量） */
    Product getDetail(Long id);

    /** 发布商品 */
    Long publish(ProductFormDTO dto);

    /** 编辑商品 */
    void update(ProductFormDTO dto);

    /** 下架商品 */
    void offline(Long id);

    /** 重新上架 */
    void online(Long id);

    /** 删除商品（逻辑删除） */
    void remove(Long id);

    /** 我发布的商品 */
    PageResult<Product> myProducts(Integer pageNum, Integer pageSize, Integer status);

    // ---------- 管理端 ----------

    PageResult<Product> adminPage(ProductQuery query);

    /** 管理员下架违规商品 */
    void adminOffline(Long id);
}
