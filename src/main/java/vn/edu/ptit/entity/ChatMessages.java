package vn.edu.ptit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "chat_messages")
public class ChatMessages implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "message_type", nullable = false)
    private String messageType = "TEXT";

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "sender_type", nullable = false)
    private String senderType;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Column(name = "is_read", nullable = false)
    private Boolean read = Boolean.FALSE;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "is_deleted", nullable = false)
    private Boolean deleted = Boolean.FALSE;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ==================== RELATIONSHIPS ====================

    /**
     * Nhiều tin nhắn thuộc 1 Chat room
     * FK: chat_messages.chat_room_id → chat_rooms.id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ChatRooms chatRoom;

    /**
     * Tin nhắn được gửi bởi User (nullable - chỉ có giá trị khi senderType = "USER")
     * FK: chat_messages.sender_user_id → users.id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_user_id", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Users senderUser;

    /**
     * Tin nhắn được gửi bởi Customer (nullable - chỉ có giá trị khi senderType = "CUSTOMER")
     * FK: chat_messages.sender_customer_id → customers.id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_customer_id", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Customers senderCustomer;
}
