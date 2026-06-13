package vn.edu.ptit.controller;

import lombok.AllArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.ptit.dto.Request.ChatAIRequest;
import vn.edu.ptit.service.AI.ChatAiHistoryService;
import vn.edu.ptit.service.AI.ChatAiService;

@RestController
@RequestMapping("/api/chat-ai")
@AllArgsConstructor
public class ChatAiController {
    private final ChatAiService chatAiService;
    private final ChatAiHistoryService chatAiHistoryService;

    @PostMapping("/answer")
    public ResponseEntity<?> getAnswer(@RequestBody ChatAIRequest chatAIRequest) {
        return ResponseEntity.ok(chatAiService.generateAnswer(chatAIRequest));
    }

    @GetMapping("/history")
    public ResponseEntity<?> getChatAiHistoryByUserId() {
        return ResponseEntity.ok(chatAiHistoryService.getChatAiHistorySummaryByUser());
    }

    @GetMapping("/history/detail")
    public ResponseEntity<?> getChatAiHistoryDetail(@Param("conversationId") String conversationId) {
        return ResponseEntity.ok(chatAiHistoryService.getChatAiHistoryByConversationId(conversationId));
    }
}
