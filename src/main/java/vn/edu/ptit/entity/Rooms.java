package vn.edu.ptit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "rooms")
public class Rooms implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_number", nullable = false)
    private String roomNumber;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "ward")
    private String ward;

    @Column(name = "district")
    private String district;

    @Column(name = "city")
    private String city;

    @Column(name = "area_m2")
    private Double areaM2;

    @Column(name = "max_occupants")
    private Long maxOccupants;

    @Column(name = "rent_price", nullable = false)
    private Double rentPrice;

    @Column(name = "electricity_price_per_kwh", nullable = false)
    private Double electricityPricePerKwh;

    @Column(name = "water_price_per_m3", nullable = false)
    private Double waterPricePerM3;

    @Column(name = "internet_fee", nullable = false)
    private Double internetFee = 0.0;

    @Column(name = "parking_fee", nullable = false)
    private Double parkingFee = 0.0;

    @Column(name = "cleaning_fee", nullable = false)
    private Double cleaningFee = 0.0;

    public enum Status {
        AVAILABLE,
        RENTED,
        MAINTENANCE
    }

    /**
     * Đổi từ String sang Enum để tránh lỗi typo
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private Status status = Status.AVAILABLE;

    @Column(name = "room_type")
    private String roomType;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ==================== RELATIONSHIPS ====================

    /**
     * Nhiều phòng thuộc 1 LandLord
     * FK: rooms.landlord_id → landlord.id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "landlord_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User landLord;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private User customer;
    /**
     * 1 Phòng có 1 hợp đồng (theo thời gian)
     */

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "room")
    @ToString.Exclude
    private Contracts contract;

    /**
     * 1 Phòng có nhiều hóa đơn điện nước
     */
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<UtilityBills> utilityBills;

    /**
     * 1 Phòng có nhiều kỳ thanh toán tiền thuê
     */
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<RentPayments> rentPayments;

    @OneToOne(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ChatRoom chatRoom;

    @OneToMany(mappedBy = "rooms", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Notifications> notifications;
}