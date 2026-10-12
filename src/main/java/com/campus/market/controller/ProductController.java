package com.campus.market.controller;

import com.campus.market.common.PageResult;
import com.campus.market.common.Result;
import com.campus.market.dto.ProductFormDTO;
import com.campus.market.dto.ProductQuery;
import com.campus.market.entity.Product;
import com.campus.market.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.campus.market.annotation.RequireLogin;

/**
 * 商品
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /** 商品列表（无需登录） */
    @GetMapping
    public Result<PageResult<Product>> list(ProductQuery query) {
        return Result.success(productService.pageOnSale(query));
    }

    /** 我发布的商品 */
    @GetMapping("/mine")
    @RequireLogin
    public Result<PageResult<Product>> mine(@RequestParam(required = false) Integer status,
                                            @RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(productService.myProducts(pageNum, pageSize, status));
    }

    /** 商品详情 */
    @GetMapping("/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        return Result.success(productService.getDetail(id));
    }

    /** 发布商品 */
    @PostMapping
    @RequireLogin
    public Result<Long> publish(@Valid @RequestBody ProductFormDTO dto) {
        return Result.success("发布成功", productService.publish(dto));
    }

    /** 编辑商品 */
    @PutMapping
    @RequireLogin
    public Result<Void> update(@Valid @RequestBody ProductFormDTO dto) {
        productService.update(dto);
        return Result.success("修改成功", null);
    }

    /** 下架 */
    @PutMapping("/{id}/offline")
    @RequireLogin
    public Result<Void> offline(@PathVariable Long id) {
        productService.offline(id);
        return Result.success("已下架", null);
    }

    /** 重新上架 */
    @PutMapping("/{id}/online")
    @RequireLogin
    public Result<Void> online(@PathVariable Long id) {
        productService.online(id);
        return Result.success("已上架", null);
    }

    /** 删除 */
    @DeleteMapping("/{id}")
    @RequireLogin
    public Result<Void> remove(@PathVariable Long id) {
        productService.remove(id);
        return Result.success("删除成功", null);
    }
}
