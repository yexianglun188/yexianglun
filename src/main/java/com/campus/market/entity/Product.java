package com.campus.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品
 */
@Data
@TableName("product")
public class Product implements Serializable {

    /** 状态：待审核 */
    public static final int STATUS_PENDING = 0;
    /** 状态：在售 */
    public static final int STATUS_ON_SALE = 1;
    /** 状态：已售 */
    public static final int STATUS_SOLD = 2;
    /** 状态：已下架 */
    public static final int STATUS_OFFLINE = 3;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 卖家id */
    private Long userId;

    private Long categoryId;

    private String title;

    private String description;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private String coverImage;

    /** 图片列表，逗号分隔 */
    private String images;

    /** 成色 */
    private String itemCondition;

    /** 交易地点 */
    private String tradePlace;

    /** 0待审核 1在售 2已售 3已下架 */
    private Integer status;

    private Integer viewCount;

    private Integer favoriteCount;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    // ------- 以下为非数据库字段，用于列表联表展示 -------

    /** 卖家昵称 */
    @TableField(exist = false)
    private String sellerNickname;

    /** 卖家头像 */
    @TableField(exist = false)
    private String sellerAvatar;

    /** 卖家所在校区 */
    @TableField(exist = false)
    private String sellerCampus;

    /** 分类名称 */
    @TableField(exist = false)
    private String categoryName;

    /** 图片列表（数组形式返回给前端） */
    @TableField(exist = false)
    private List<String> imageList;

    /** 当前登录用户是否已收藏 */
    @TableField(exist = false)
    private Boolean favorited;
}
