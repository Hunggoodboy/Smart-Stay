package vn.edu.ptit.controller;

import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import vn.edu.ptit.dto.Request.ChatMessageRequest;
import vn.edu.ptit.service.ChatMessageService;

@Controller
@AllArgsConstructor
public class ChatController {
    private final ChatMessageService chatMessageService;

    @MessageMapping("/chat.private")
    public void sendPrivateMessage(@Payload ChatMessageRequest chatMessageRequest) {
        chatMessageService.sendPrivateMessage(chatMessageRequest);
    }

    @MessageMapping("/chat.history")
    public void getChatHistory(@Payload ChatMessageRequest chatMessageRequest) {
        Long senderId = chatMessageRequest.getSenderId();
        Long receiverId = chatMessageRequest.getReceiverId();
        // Lấy lịch sử trò chuyện giữa sender và receiver
        chatMessageService.loadMessageHistory(senderId, receiverId);
    }

}
