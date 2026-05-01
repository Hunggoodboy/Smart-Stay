package vn.edu.ptit.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import vn.edu.ptit.dto.Request.ChatMessageRequest;
import vn.edu.ptit.dto.Response.ConversationResponse;
import vn.edu.ptit.entity.User;
import vn.edu.ptit.repository.UserRepository;
import vn.edu.ptit.service.Authentication.AuthService;
import vn.edu.ptit.service.ChatMessageService;

import java.security.Principal;
import java.util.List;

@Controller
@AllArgsConstructor
public class ChatController {
    private final ChatMessageService chatMessageService;
    private final UserRepository userRepository;
    private final AuthService authService;

    @MessageMapping("/chat.private")
    public void sendPrivateMessage(@Payload ChatMessageRequest chatMessageRequest) {
        chatMessageService.sendPrivateMessage(chatMessageRequest);
    }


    @MessageMapping("/chat.history")
    public void getChatHistory(@Payload ChatMessageRequest chatMessageRequest) {
        Long senderId = chatMessageRequest.getSenderId();
        Long receiverId = chatMessageRequest.getReceiverId();
        if (senderId == null) {
            User sender = userRepository.findById(authService.getCurrentUserId())
                    .orElseThrow(() -> new RuntimeException("Sender not found"));
            senderId = sender.getId();
        }
        System.out.println(receiverId);
        chatMessageService.loadMessageHistory(senderId, receiverId);
    }
    @GetMapping("/api/chat/conversations")
    public ResponseEntity<List<ConversationResponse>> getConversations() {
        return ResponseEntity.ok(chatMessageService.getConversations());
    }
}
