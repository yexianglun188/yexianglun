package com.campus.market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.market.common.BusinessException;
import com.campus.market.common.PageResult;
import com.campus.market.common.ResultCode;
import com.campus.market.entity.Favorite;
import com.campus.market.entity.Product;
import com.campus.market.mapper.FavoriteMapper;
import com.campus.market.mapper.ProductMapper;
import com.campus.market.service.FavoriteService;
import com.campus.market.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final ProductMapper productMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggle(Long productId) {
        Long userId = UserContext.requireUserId();
        if (productMapper.selectById(productId) == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "商品不存在");
        }

        Favorite exists = favoriteMapper.selectOne(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId));

        if (exists != null) {
            favoriteMapper.deleteById(exists.getId());
            updateCount(productId, -1);
            return false;
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setProductId(productId);
        favorite.setCreateTime(LocalDateTime.now());
        favoriteMapper.insert(favorite);
        updateCount(productId, 1);
        return true;
    }

    @Override
    public PageResult<Favorite> myFavorites(Integer pageNum, Integer pageSize) {
        Long userId = UserContext.requireUserId();
        Page<Favorite> page = new Page<>(pageNum == null ? 1 : pageNum, pageSize == null ? 10 : pageSize);
        IPage<Favorite> result = favoriteMapper.selectFavoritePage(page, userId);
        return PageResult.of(result.getTotal(), result.getRecords());
    }

    /** 收藏数增减，使用 SQL 自增保证并发安全 */
    private void updateCount(Long productId, int delta) {
        String sql = delta > 0 ? "favorite_count = favorite_count + 1"
                : "favorite_count = GREATEST(favorite_count - 1, 0)";
        productMapper.update(null, new LambdaUpdateWrapper<Product>()
                .eq(Product::getId, productId)
                .setSql(sql));
    }
}
