package com.campus.market.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话摘要（用于会话列表展示）
 */
@Data
public class ConversationVO {

    /** 对方用户id */
    private Long peerId;

    /** 关联商品id（可选） */
    private Long productId;

    /** 对方昵称 */
    private String peerNickname;

    /** 对方头像 */
    private String peerAvatar;

    /** 关联商品标题 */
    private String productTitle;

    /** 关联商品封面 */
    private String productCover;

    /** 最后一条消息内容 */
    private String lastContent;

    /** 最后一条消息时间 */
    private LocalDateTime lastTime;

    /** 本会话中我的未读消息数 */
    private Long unread;
}
