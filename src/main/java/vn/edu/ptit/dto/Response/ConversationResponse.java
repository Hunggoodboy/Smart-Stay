package vn.edu.ptit.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConversationResponse {
    private Long partnerId;
    private String partnerName;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private Integer unreadCount;
}
