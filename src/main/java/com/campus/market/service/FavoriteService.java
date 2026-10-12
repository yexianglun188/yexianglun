package com.campus.market.service;

import com.campus.market.common.PageResult;
import com.campus.market.entity.Favorite;

public interface FavoriteService {

    /** 收藏 / 取消收藏，返回操作后是否已收藏 */
    boolean toggle(Long productId);

    /** 我的收藏列表 */
    PageResult<Favorite> myFavorites(Integer pageNum, Integer pageSize);
}
