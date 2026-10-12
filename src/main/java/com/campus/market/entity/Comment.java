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
 * 商品留言
 */
@Data
@TableName("comment")
public class Comment implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productId;

    private Long userId;

    private String content;

    /** 父留言id，0为顶级留言 */
    private Long parentId;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    // ------- 非数据库字段 -------

    @TableField(exist = false)
    private String userNickname;

    @TableField(exist = false)
    private String userAvatar;
}
