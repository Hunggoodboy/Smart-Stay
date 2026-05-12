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
     * Thời điểm chủ nhà duyệt hoặc từ chối.
     */
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at", nullable = true)
    private LocalDateTime deletedAt;


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

    @OneToOne(mappedBy = "rentalRequest", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Contracts contract;
}
