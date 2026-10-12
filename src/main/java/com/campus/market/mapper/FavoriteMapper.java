package com.campus.market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.market.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    /** 分页查询我的收藏（联表带出商品信息） */
    IPage<Favorite> selectFavoritePage(IPage<Favorite> page, @Param("userId") Long userId);
}
