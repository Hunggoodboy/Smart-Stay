package vn.edu.ptit.dto.Response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatMessagesResponse {
    private Long id;
    private Long chatRoomId;
    private String content;
    private String chatType;
    private String messageType;
    private String senderType;
    private Long senderId;
    private String senderName;
    private Long receiverId; // null nếu là chat Room
    private LocalDateTime sentAt;
}
