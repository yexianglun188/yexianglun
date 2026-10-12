package com.campus.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 私信消息
 */
@Data
@TableName("message")
public class Message implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发送者id */
    private Long fromId;

    /** 接收者id */
    private Long toId;

    /** 关联商品id（可选，用于区分不同商品下的会话） */
    private Long productId;

    /** 消息内容 */
    private String content;

    /** 是否已读 0未读 1已读（Java 字段名避开 MySQL 保留字 read；数据库列名映射为 is_read） */
    @TableField("is_read")
    private Integer isRead;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    // ------- 非数据库字段，仅用于联表展示 -------

    /** 发送者昵称 */
    @TableField(exist = false)
    private String fromNickname;

    /** 发送者头像 */
    @TableField(exist = false)
    private String fromAvatar;

    /** 接收者昵称 */
    @TableField(exist = false)
    private String toNickname;

    /** 接收者头像 */
    @TableField(exist = false)
    private String toAvatar;

    /** 关联商品标题 */
    @TableField(exist = false)
    private String productTitle;

    /** 关联商品封面 */
    @TableField(exist = false)
    private String productCover;
}
