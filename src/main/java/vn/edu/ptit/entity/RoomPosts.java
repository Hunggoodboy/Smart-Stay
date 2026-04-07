package vn.edu.ptit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Bài đăng cho thuê phòng — do LandLord tạo ĐỘC LẬP, chưa liên kết Rooms.
 *
 * Flow vòng đời:
 *   LandLord tạo bài đăng (DRAFT)
 *       → publish (ACTIVE)
 *       → Customer gửi RentalRequest
 *       → LandLord duyệt (RENTED) → hệ thống tự tạo Rooms + Contracts
 *       → Phòng trống lại → LandLord đăng bài mới
 *
 * Khi status chuyển sang RENTED, field `room` sẽ được gán (tham chiếu Rooms vừa tạo).
 */
@Entity
@Data
@Table(name = "room_posts")
public class RoomPosts implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==================== THÔNG TIN CƠ BẢN ====================

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "monthly_rent", nullable = false)
    private BigDecimal monthlyRent;

    @Column(name = "deposit_amount")
    private BigDecimal depositAmount;

    @Column(name = "area_m2")
    private Double areaM2;

    @Column(name = "max_occupants")
    private Integer maxOccupants;

    @Column(name = "room_type")
    private String roomType;

    // ==================== ĐỊA CHỈ ====================

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "ward")
    private String ward;

    @Column(name = "district")
    private String district;

    @Column(name = "city", nullable = false)
    private String city;


    // ==================== GIÁ DỊCH VỤ (dùng để tạo Rooms khi cho thuê) ====================

    @Column(name = "electricity_price_per_kwh", nullable = false)
    private BigDecimal electricityPricePerKwh = new BigDecimal("3500");

    @Column(name = "water_price_per_m3", nullable = false)
    private BigDecimal waterPricePerM3 = new BigDecimal("15000");

    @Column(name = "internet_fee", nullable = false)
    private Double internetFee = 0.0;

    @Column(name = "parking_fee", nullable = false)
    private Double parkingFee = 0.0;

    @Column(name = "cleaning_fee", nullable = false)
    private Double cleaningFee = 0.0;

    // ==================== TRẠNG THÁI ====================

    public enum Status {
        /** Bản nháp, chưa công khai */
        DRAFT,
        /** Đang hiển thị công khai */
        ACTIVE,
        /** Tạm ẩn bởi landlord */
        INACTIVE,
        /** Đã có người thuê — Rooms + Contracts đã được tạo */
        RENTED,
        /** Đã xoá mềm */
        DELETED
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private Status status = Status.DRAFT;


    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "main_image_url")
    private String mainImageUrl;
    // ==================== RELATIONSHIPS ====================

    /**
     * Người đăng bài.
     * FK: room_posts.landlord_id → landlords.user_id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "landlord_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private LandLord landLord;

    /**
     * Phòng được tạo ra sau khi bài đăng này chuyển sang RENTED.
     * NULL cho đến khi hệ thống tạo Rooms từ bài đăng này.
     * FK: room_posts.room_id → rooms.id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Rooms room;

    /**
     * Danh sách ảnh của bài đăng.
     */
    @OneToMany(mappedBy = "roomPost", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<RoomPostImages> images = new ArrayList<>();

    /**
     * Danh sách yêu cầu thuê từ khách hàng.
     */
    @OneToMany(mappedBy = "roomPost", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<RentalRequests> rentalRequests = new ArrayList<>();
}
