package com.campus.market.controller;

import com.campus.market.annotation.RequireLogin;
import com.campus.market.common.PageResult;
import com.campus.market.common.Result;
import com.campus.market.entity.Favorite;
import com.campus.market.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 收藏
 */
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /** 收藏 / 取消收藏 */
    @PostMapping("/toggle/{productId}")
    @RequireLogin
    public Result<Boolean> toggle(@PathVariable Long productId) {
        boolean favorited = favoriteService.toggle(productId);
        return Result.success(favorited ? "收藏成功" : "已取消收藏", favorited);
    }

    /** 我的收藏 */
    @GetMapping
    @RequireLogin
    public Result<PageResult<Favorite>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(favoriteService.myFavorites(pageNum, pageSize));
    }
}
