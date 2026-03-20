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

    @Column(name = "is_read", nullable = true)
    private Boolean isRead = false;

    @Column(name = "message_type", nullable = false)
    private String messageType = "TEXT";

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

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
    @JoinColumn(name = "chat_room_id", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ChatRoom chatRoom;

    // ==================== LandLord chủ nhà ====================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_landlord_id", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private LandLord landLord;


    /**
     * Tin nhắn được gửi bởi Customer (nullable - chỉ có giá trị khi senderType = "CUSTOMER")
     * FK: chat_messages.sender_customer_id → customers.id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User receiver;
}
