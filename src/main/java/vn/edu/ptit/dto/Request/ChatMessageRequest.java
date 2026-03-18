package vn.edu.ptit.dto.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import vn.edu.ptit.entity.ChatMessages;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@ToString
public class ChatMessageRequest {
    private Long id;
    private Long chatRoomId;
    private String content;
    private String messageType;
    private String senderType;
    private Long senderId;
    private Long receiverId; // null nếu là chat Room
    private LocalDateTime sentAt;
}
