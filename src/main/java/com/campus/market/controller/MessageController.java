package com.campus.market.controller;

import com.campus.market.annotation.RequireLogin;
import com.campus.market.common.Result;
import com.campus.market.dto.ConversationVO;
import com.campus.market.dto.MessageCreateDTO;
import com.campus.market.entity.Message;
import com.campus.market.service.MessageService;
import com.campus.market.util.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 站内私信
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /** 我的会话列表 */
    @GetMapping("/conversations")
    @RequireLogin
    public Result<List<ConversationVO>> conversations() {
        return Result.success(messageService.listConversations(UserContext.requireUserId()));
    }

    /** 与某用户的会话消息（按商品区分） */
    @GetMapping
    @RequireLogin
    public Result<List<Message>> list(@RequestParam Long peerId,
                                      @RequestParam(required = false) Long productId) {
        return Result.success(messageService.listMessages(UserContext.requireUserId(), peerId, productId));
    }

    /** 发送私信 */
    @PostMapping
    @RequireLogin
    public Result<Message> send(@Valid @RequestBody MessageCreateDTO dto) {
        return Result.success("发送成功", messageService.send(UserContext.requireUserId(), dto));
    }

    /** 我的未读消息总数 */
    @GetMapping("/unread")
    @RequireLogin
    public Result<Long> unread() {
        return Result.success(messageService.unreadCount(UserContext.requireUserId()));
    }
}
