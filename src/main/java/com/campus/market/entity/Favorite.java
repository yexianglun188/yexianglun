package com.campus.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 收藏
 */
@Data
@TableName("favorite")
public class Favorite implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long productId;

    private LocalDateTime createTime;

    // ------- 非数据库字段（收藏列表展示商品信息） -------

    @TableField(exist = false)
    private String productTitle;

    @TableField(exist = false)
    private String productCover;

    @TableField(exist = false)
    private BigDecimal productPrice;

    @TableField(exist = false)
    private Integer productStatus;
}
