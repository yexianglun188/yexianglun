package com.campus.market.service.impl;

import com.campus.market.common.BusinessException;
import com.campus.market.common.ResultCode;
import com.campus.market.dto.CommentCreateDTO;
import com.campus.market.entity.Comment;
import com.campus.market.mapper.CommentMapper;
import com.campus.market.service.CommentService;
import com.campus.market.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;

    @Override
    public List<Comment> listByProduct(Long productId) {
        return commentMapper.selectByProductId(productId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(CommentCreateDTO dto) {
        Comment comment = new Comment();
        comment.setProductId(dto.getProductId());
        comment.setUserId(UserContext.requireUserId());
        comment.setContent(dto.getContent());
        comment.setParentId(dto.getParentId() == null ? 0L : dto.getParentId());
        comment.setCreateTime(LocalDateTime.now());
        commentMapper.insert(comment);
        return comment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "留言不存在");
        }
        // 本人或管理员可删除
        if (!comment.getUserId().equals(UserContext.getUserId()) && !UserContext.isAdmin()) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权删除该留言");
        }
        commentMapper.deleteById(id);
    }
}
