package vn.edu.ptit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "chat_rooms")
public class ChatRoom implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_post_id")
    private Long roomPostId;

    @Column(name = "room_key", nullable = false, unique = true)
    private String roomKey;

    @Column(name = "last_message")
    private String lastMessage;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "user_unread_count", nullable = false)
    private Long userUnreadCount = 0L;

    @Column(name = "customer_unread_count", nullable = false)
    private Long customerUnreadCount = 0L;

    @Column(name = "is_active", nullable = false)
    private Boolean active = Boolean.FALSE;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "sender_type", nullable = false)
    private String senderType; // "CUSTOMER" hoặc "LANDLORD"

    // ==================== RELATIONSHIPS ====================

    /**
     * Nhiều chat room thuộc 1 Customer (người thuê)
     * FK: chat_rooms.user_id → users.id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Customer customer;

    /**
     * Nhiều chat room thuộc 1 LandLord (chủ nhà)
     * FK: chat_rooms.customer_id → customers.id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "landlord_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private LandLord landLord;

    /**
     * 1 Chat room chứa nhiều tin nhắn
     */
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ChatMessages> messages;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = true)
    private Rooms room;
}
