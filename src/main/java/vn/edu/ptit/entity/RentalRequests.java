package vn.edu.ptit.dto.Request;

import jakarta.persistence.*;
import lombok.*;
import vn.edu.ptit.entity.Contracts;
import vn.edu.ptit.entity.Customer;
import vn.edu.ptit.entity.LandLord;
import vn.edu.ptit.entity.RoomPosts;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Yêu cầu thuê phòng — do Customer gửi sau khi xem bài đăng.
 * Flow: PENDING → APPROVED (→ tạo Contract) | REJECTED | CANCELLED
 */
@Entity
@Data
@Table(
    name = "rental_requests",
    uniqueConstraints = {
        // Mỗi khách chỉ được có 1 yêu cầu đang PENDING/APPROVED cho 1 bài đăng
        @UniqueConstraint(
            name = "uq_rental_request_active",
            columnNames = {"room_post_id", "customer_id", "status"}
        )
    }
)
public class RentalRequests implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Lời nhắn từ khách hàng gửi cho chủ nhà
     */
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    /**
     * Ngày khách muốn chuyển vào (dự kiến)
     */
    @Column(name = "desired_move_in_date")
    private LocalDate desiredMoveInDate;

    /**
     * Số tháng dự kiến thuê
     */
    @Column(name = "desired_duration_months")
    private Integer desiredDurationMonths;

    /**
     * Số người dự kiến ở
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
        /** Đã hoàn tất — Contract đã được tạo từ yêu cầu này */
        CONTRACTED
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private Status status = Status.PENDING;

    /**
     * Lý do từ chối hoặc huỷ (điền khi status = REJECTED | CANCELLED)
     */
    @Column(name = "rejection_reason")
    private String rejectionReason;

    /**
     * Ghi chú nội bộ của chủ nhà
     */
    @Column(name = "landlord_notes")
    private String landlordNotes;

    /**
     * Thời điểm chủ nhà duyệt / từ chối
     */
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ==================== RELATIONSHIPS ====================

    /**
     * Yêu cầu thuộc bài đăng nào
     * FK: rental_requests.room_post_id → room_posts.id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_post_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private RoomPosts roomPost;

    /**
     * Khách hàng gửi yêu cầu
     * FK: rental_requests.customer_id → customers.user_id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Customer customer;

    /**
     * Chủ nhà nhận yêu cầu (denormalized để query nhanh, thực ra lấy từ roomPost.landLord)
     * FK: rental_requests.landlord_id → landlords.user_id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "landlord_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private LandLord landLord;

    /**
     * Contract được tạo từ yêu cầu này (nullable — chỉ có khi status = CONTRACTED)
     * FK: rental_requests.contract_id → contracts.id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Contracts contract;
}
