package vn.edu.ptit.service.AI;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.ptit.dto.Response.ChatAiHistoryResponse;
import vn.edu.ptit.dto.Response.ChatAiSummaryResponse;
import vn.edu.ptit.entity.User;
import vn.edu.ptit.repository.ChatAiHistoryRepository;
import vn.edu.ptit.service.Authentication.AuthService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ChatAiHistoryService {
    private final ChatAiHistoryRepository chatAiHistoryRepository;
    private final AuthService authService;
    public List<ChatAiHistoryResponse> getChatAiHistoryByConversationId(String conversationId) {
        Long userId = authService.getCurrentUserId();
        return chatAiHistoryRepository.findByConversationIdAndUserIdOrderByCreatedAtDesc(conversationId, userId).stream()
                .map(chatAiHistory -> {
                    return ChatAiHistoryResponse.builder()
                            .message(chatAiHistory.getContent())
                            .message_type(chatAiHistory.getMessageType())
                            .build();
                })
                .collect(Collectors.toList());
    }
    public List<ChatAiSummaryResponse> getChatAiHistorySummaryByUser() {
        Long userId = authService.getCurrentUserId();
        return chatAiHistoryRepository.getChatAiHistoryByUserId(userId).stream()
                .map(chatAi -> {
                    return ChatAiSummaryResponse.builder()
                            .lastChatTime(chatAi.getCreatedAt())
                            .conversationId(chatAi.getConversationId())
                            .lastChatMessage(chatAi.getContent())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
