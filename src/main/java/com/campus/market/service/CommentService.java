package com.campus.market.service;

import com.campus.market.dto.CommentCreateDTO;
import com.campus.market.entity.Comment;

import java.util.List;

public interface CommentService {

    List<Comment> listByProduct(Long productId);

    Long add(CommentCreateDTO dto);

    void delete(Long id);
}
