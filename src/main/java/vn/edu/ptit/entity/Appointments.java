package vn.edu.ptit.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "appointments")
public class Appointments implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==================== THÔNG TIN CHUNG ====================

    @Column(name = "appointment_time", nullable = false)
    private LocalDateTime appointmentTime;

    @Column(name = "location")
    private String location;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    public enum Status {
        /** Chờ xác nhận */
        PENDING,
        /** Đã xác nhận bởi người thuê */
        CONFIRMED_BY_TENANT,
        /** Đã xác nhận bởi chủ nhà */
        CONFIRMED_BY_LANDLORD,
        /** Đã xác nhận bởi cả hai bên */
        CONFIRMED_BY_BOTH,
        /** Đã hoàn tất */
        COMPLETED,
        /** Đã huỷ */
        CANCELLED
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private Status status = Status.PENDING;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User createdBy;

    // ==================== RELATIONSHIPS ====================

    /**
     * Lịch hẹn thuộc về một yêu cầu thuê cụ thể.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_request_id", nullable = false, unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private RentalRequests rentalRequest;

    /**
     * Khách hàng tham gia lịch hẹn.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Customer customer;

    /**
     * Chủ nhà tham gia lịch hẹn.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "landlord_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private LandLord landlord;

    /**
     * Bài đăng phòng trọ liên quan đến lịch hẹn.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_post_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private RoomPosts roomPost;
}
