package com.campus.market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.campus.market.common.BusinessException;
import com.campus.market.common.ResultCode;
import com.campus.market.dto.ConversationVO;
import com.campus.market.dto.MessageCreateDTO;
import com.campus.market.entity.Message;
import com.campus.market.entity.Product;
import com.campus.market.entity.User;
import com.campus.market.mapper.MessageMapper;
import com.campus.market.mapper.ProductMapper;
import com.campus.market.mapper.UserMapper;
import com.campus.market.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;

    @Override
    public List<ConversationVO> listConversations(Long userId) {
        List<Message> latest = messageMapper.selectLatestConversations(userId);
        List<ConversationVO> result = new ArrayList<>();
        for (Message m : latest) {
            Long peerId = m.getFromId().equals(userId) ? m.getToId() : m.getFromId();

            ConversationVO vo = new ConversationVO();
            vo.setPeerId(peerId);
            vo.setProductId(m.getProductId());
            vo.setLastContent(m.getContent());
            vo.setLastTime(m.getCreateTime());

            User peer = userMapper.selectById(peerId);
            if (peer != null) {
                vo.setPeerNickname(peer.getNickname());
                vo.setPeerAvatar(peer.getAvatar());
            }
            if (m.getProductId() != null) {
                Product p = productMapper.selectById(m.getProductId());
                if (p != null) {
                    vo.setProductTitle(p.getTitle());
                    vo.setProductCover(p.getCoverImage());
                }
            }

            QueryWrapper<Message> uq = new QueryWrapper<>();
            uq.eq("to_id", userId).eq("from_id", peerId).eq("is_read", 0).eq("deleted", 0);
            if (m.getProductId() != null) uq.eq("product_id", m.getProductId());
            else uq.isNull("product_id");
            vo.setUnread(messageMapper.selectCount(uq));

            result.add(vo);
        }
        return result;
    }

    @Override
    public List<Message> listMessages(Long userId, Long peerId, Long productId) {
        QueryWrapper<Message> qw = new QueryWrapper<>();
        qw.and(w -> w.eq("from_id", userId).eq("to_id", peerId)
                     .or().eq("from_id", peerId).eq("to_id", userId));
        if (productId != null) qw.eq("product_id", productId);
        else qw.isNull("product_id");
        qw.eq("deleted", 0).orderByAsc("id");

        List<Message> list = messageMapper.selectList(qw);

        // 把我收到的未读消息标记为已读
        UpdateWrapper<Message> uw = new UpdateWrapper<>();
        uw.eq("to_id", userId).eq("from_id", peerId).eq("is_read", 0).eq("deleted", 0);
        if (productId != null) uw.eq("product_id", productId);
        else uw.isNull("product_id");
        uw.set("is_read", 1);
        messageMapper.update(null, uw);

        // 填充发送方昵称与头像，供前端渲染
        for (Message msg : list) {
            User u = userMapper.selectById(msg.getFromId());
            if (u != null) {
                msg.setFromNickname(u.getNickname());
                msg.setFromAvatar(u.getAvatar());
            }
        }
        return list;
    }

    @Override
    public Message send(Long userId, MessageCreateDTO dto) {
        if (dto.getToId() == null || dto.getToId().equals(userId)) {
            throw new BusinessException("消息接收者不合法");
        }
        User to = userMapper.selectById(dto.getToId());
        if (to == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "接收用户不存在");
        }

        Message msg = new Message();
        msg.setFromId(userId);
        msg.setToId(dto.getToId());
        msg.setProductId(dto.getProductId());
        msg.setContent(dto.getContent().trim());
        msg.setIsRead(0);
        messageMapper.insert(msg);

        User me = userMapper.selectById(userId);
        if (me != null) {
            msg.setFromNickname(me.getNickname());
            msg.setFromAvatar(me.getAvatar());
        }
        return msg;
    }

    @Override
    public Long unreadCount(Long userId) {
        QueryWrapper<Message> qw = new QueryWrapper<>();
        qw.eq("to_id", userId).eq("is_read", 0).eq("deleted", 0);
        return messageMapper.selectCount(qw);
    }
}
