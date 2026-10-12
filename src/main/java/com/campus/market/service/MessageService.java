package com.campus.market.service;

import com.campus.market.dto.ConversationVO;
import com.campus.market.dto.MessageCreateDTO;
import com.campus.market.entity.Message;

import java.util.List;

public interface MessageService {

    /** 会话列表（按最近消息倒序，含未读数与对方/商品信息） */
    List<ConversationVO> listConversations(Long userId);

    /** 与指定用户的会话消息（按商品区分），查看时自动标记已读 */
    List<Message> listMessages(Long userId, Long peerId, Long productId);

    /** 发送私信 */
    Message send(Long userId, MessageCreateDTO dto);

    /** 当前用户未读消息总数 */
    Long unreadCount(Long userId);
}
