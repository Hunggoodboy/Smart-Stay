package vn.edu.ptit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "notifications")
public class Notifications implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Loại người nhận: "USER" | "CUSTOMER" | "ADMIN"
     * Dùng để xác định bảng cần join khi lấy thông tin người nhận
     */
    @Column(name = "recipient_type", nullable = false)
    private String recipientType;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "notification_type", nullable = false)
    private String notificationType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "is_read", nullable = false)
    private Boolean read = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ==================== RELATIONSHIPS ====================
    // Notifications dùng kiểu "polymorphic" - 1 recipientId có thể trỏ đến
    // users, customers hoặc admins tùy recipientType.
    // Vì JPA không hỗ trợ polymorphic FK trực tiếp, ta tách thành 2 FK nullable.

    /**
     * Người nhận là User (nullable - chỉ có giá trị khi recipientType = "USER")
     * FK: notifications.recipient_id → users.id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id",
                nullable = true,
                insertable = false,
                updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    /**
     * Người nhận là Customer (nullable - chỉ có giá trị khi recipientType = "CUSTOMER")
     * FK: notifications.recipient_id → customers.id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id",
                nullable = true,
                insertable = false,
                updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Customers customer;

    /**
     * recipient_id - cột thực tế lưu ID người nhận
     * Dùng để set giá trị khi lưu (vì 2 field trên đều insertable=false)
     */
    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;
}
