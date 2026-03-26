package vn.edu.ptit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Bài đăng cho thuê phòng — do LandLord tạo từ một Rooms đã có.
 * Một phòng có thể có nhiều bài đăng theo thời gian (mỗi lần phòng trống lại đăng mới).
 */
@Entity
@Data
@Table(name = "room_posts")
public class RoomPosts implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Giá hiển thị trên bài đăng (có thể khác giá gốc trong Rooms)
     */
    @Column(name = "posted_price", nullable = false)
    private BigDecimal postedPrice;

    @Column(name = "area_m2")
    private Double areaM2;

    @Column(name = "room_type")
    private String roomType;

    // ==================== ĐỊA CHỈ (denormalized để tìm kiếm nhanh) ====================

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "ward")
    private String ward;

    @Column(name = "district")
    private String district;

    @Column(name = "city")
    private String city;

    // ==================== TIỆN ÍCH ====================

    @Column(name = "has_wifi", nullable = false)
    private Boolean hasWifi = false;

    @Column(name = "has_air_conditioner", nullable = false)
    private Boolean hasAirConditioner = false;

    @Column(name = "has_water_heater", nullable = false)
    private Boolean hasWaterHeater = false;

    @Column(name = "has_parking", nullable = false)
    private Boolean hasParking = false;

    @Column(name = "has_security", nullable = false)
    private Boolean hasSecurity = false;

    @Column(name = "has_elevator", nullable = false)
    private Boolean hasElevator = false;

    @Column(name = "allow_cooking", nullable = false)
    private Boolean allowCooking = false;

    @Column(name = "allow_pet", nullable = false)
    private Boolean allowPet = false;

    /**
     * Tiện ích bổ sung dạng tự do (VD: "Ban công, Tủ lạnh, Máy giặt")
     */
    @Column(name = "extra_amenities")
    private String extraAmenities;

    // ==================== PHÍ DỊCH VỤ HIỂN THỊ ====================

    @Column(name = "electricity_price_per_kwh")
    private BigDecimal electricityPricePerKwh;

    @Column(name = "water_price_per_m3")
    private BigDecimal waterPricePerM3;

    @Column(name = "internet_fee")
    private Double internetFee = 0.0;

    @Column(name = "parking_fee")
    private Double parkingFee = 0.0;

    // ==================== THỐNG KÊ ====================

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @Column(name = "contact_count", nullable = false)
    private Long contactCount = 0L;

    // ==================== TRẠNG THÁI ====================

    public enum Status {
        /** Bản nháp, chưa công khai */
        DRAFT,
        /** Đang hiển thị công khai */
        ACTIVE,
        /** Tạm ẩn */
        INACTIVE,
        /** Đã có người thuê, ẩn khỏi danh sách */
        RENTED,
        /** Đã bị xóa mềm */
        DELETED
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private Status status = Status.DRAFT;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ==================== RELATIONSHIPS ====================

    /**
     * Bài đăng thuộc về phòng nào
     * FK: room_posts.room_id → rooms.id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Rooms room;

    /**
     * Người đăng bài
     * FK: room_posts.landlord_id → landlords.user_id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "landlord_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private LandLord landLord;

    /**
     * Danh sách ảnh của bài đăng
     */
    @OneToMany(mappedBy = "roomPost", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<RoomPostImages> images = new ArrayList<>();

    /**
     * Danh sách yêu cầu thuê từ khách hàng
     */
    @OneToMany(mappedBy = "roomPost", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<RentalRequests> rentalRequests = new ArrayList<>();
}
