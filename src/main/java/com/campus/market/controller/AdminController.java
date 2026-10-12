package com.campus.market.controller;

import com.campus.market.annotation.RequireAdmin;
import com.campus.market.common.PageResult;
import com.campus.market.common.Result;
import com.campus.market.dto.DashboardVO;
import com.campus.market.dto.ProductQuery;
import com.campus.market.entity.Product;
import com.campus.market.entity.User;
import com.campus.market.service.DashboardService;
import com.campus.market.service.ProductService;
import com.campus.market.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台管理（仅管理员）
 */
@RestController
@RequestMapping("/api/admin")
@RequireAdmin
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final ProductService productService;
    private final DashboardService dashboardService;

    /** 数据看板统计 */
    @GetMapping("/dashboard")
    public Result<DashboardVO> dashboard() {
        return Result.success(dashboardService.stats());
    }

    /** 用户列表 */
    @GetMapping("/users")
    public Result<PageResult<User>> users(@RequestParam(defaultValue = "1") Integer pageNum,
                                          @RequestParam(defaultValue = "10") Integer pageSize,
                                          @RequestParam(required = false) String keyword) {
        return Result.success(userService.pageUsers(pageNum, pageSize, keyword));
    }

    /** 启用 / 禁用用户 */
    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return Result.success(status == 1 ? "已启用" : "已禁用", null);
    }

    /** 商品列表（含各状态） */
    @GetMapping("/products")
    public Result<PageResult<Product>> products(ProductQuery query) {
        return Result.success(productService.adminPage(query));
    }

    /** 下架违规商品 */
    @PutMapping("/products/{id}/offline")
    public Result<Void> offlineProduct(@PathVariable Long id) {
        productService.adminOffline(id);
        return Result.success("已下架该商品", null);
    }
}
