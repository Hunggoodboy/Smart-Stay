package vn.edu.ptit.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "rental_requests")
public class RentalRequests implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Lời nhắn từ khách hàng gửi chủ nhà.
     */
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    /**
     * Ngày khách dự kiến chuyển vào.
     */
    @Column(name = "desired_move_in_date")
    private LocalDate desiredMoveInDate;

    /**
     * Số tháng dự kiến thuê.
     */
    @Column(name = "desired_duration_months")
    private Integer desiredDurationMonths;

    /**
     * Số người dự kiến ở cùng.
     */
    @Column(name = "num_occupants", nullable = false)
    private Integer numOccupants = 1;

    // ==================== TRẠNG THÁI ====================

    public enum Status {
        /** Chờ chủ nhà xử lý */
        PENDING,
        /** Chủ nhà đã duyệt, đang chờ tạo hợp đồng */
        APPROVED,
        /** Chủ nhà từ chối */
        REJECTED,
        /** Khách hàng tự huỷ */
        CANCELLED,
        /** Đã hoàn tất — Rooms + Contracts đã được tạo */
        CONTRACTED
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private Status status = Status.PENDING;

    /**
     * Lý do từ chối (điền khi status = REJECTED).
     */
    @Column(name = "rejection_reason")
    private String rejectionReason;

    /**
     * Ghi chú nội bộ của chủ nhà (không hiển thị cho khách).
     */
    @Column(name = "landlord_notes")
    private String landlordNotes;

    /**
     * Thời điểm chủ nhà duyệt hoặc từ chối.
     */
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ==================== RELATIONSHIPS ====================

    /**
     * Yêu cầu thuộc bài đăng nào.
     * FK: rental_requests.room_post_id → room_posts.id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_post_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private RoomPosts roomPost;

    /**
     * Khách hàng gửi yêu cầu.
     * FK: rental_requests.customer_id → customers.user_id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Customer customer;

    /**
     * Chủ nhà nhận yêu cầu (denormalized để tránh join qua roomPost).
     * FK: rental_requests.landlord_id → landlords.user_id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "landlord_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private LandLord landlord;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Contracts contract;
}
