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

/**
 * 订单
 */
@Data
@TableName("orders")
public class Orders implements Serializable {

    /** 待支付（下单后尚未付款） */
    public static final int STATUS_UNPAID = 0;
    /** 待卖家确认（已支付） */
    public static final int STATUS_PENDING = 1;
    /** 交易中（卖家已确认） */
    public static final int STATUS_TRADING = 2;
    /** 已完成 */
    public static final int STATUS_FINISHED = 3;
    /** 已取消 */
    public static final int STATUS_CANCELED = 4;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long productId;

    private Long buyerId;

    private Long sellerId;

    private BigDecimal amount;

    /** 0待确认 1交易中 2已完成 3已取消 */
    private Integer status;

    private String remark;

    private String contact;

    /** 支付时间 */
    private LocalDateTime paidTime;

    @TableLogic
    private Integer deleted;

    private LocalDateTime finishTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    // ------- 非数据库字段 -------

    @TableField(exist = false)
    private String productTitle;

    @TableField(exist = false)
    private String productCover;

    @TableField(exist = false)
    private String buyerNickname;

    @TableField(exist = false)
    private String sellerNickname;
}
