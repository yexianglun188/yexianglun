package com.campus.market.controller;

import com.campus.market.annotation.RequireLogin;
import com.campus.market.common.Result;
import com.campus.market.dto.CommentCreateDTO;
import com.campus.market.entity.Comment;
import com.campus.market.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商品留言
 */
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /** 某商品的留言列表 */
    @GetMapping("/product/{productId}")
    public Result<List<Comment>> list(@PathVariable Long productId) {
        return Result.success(commentService.listByProduct(productId));
    }

    /** 发表留言 */
    @PostMapping
    @RequireLogin
    public Result<Long> add(@Valid @RequestBody CommentCreateDTO dto) {
        return Result.success("留言成功", commentService.add(dto));
    }

    /** 删除留言（本人或管理员） */
    @DeleteMapping("/{id}")
    @RequireLogin
    public Result<Void> delete(@PathVariable Long id) {
        commentService.delete(id);
        return Result.success("删除成功", null);
    }
}
