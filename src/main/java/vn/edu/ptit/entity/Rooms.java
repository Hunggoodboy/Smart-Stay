package vn.edu.ptit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
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
    private BigDecimal rentPrice;

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

    @Column(name = "status", nullable = false)
    private String status = "AVAILABLE";

    @Column(name = "room_type")
    private String roomType;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ==================== RELATIONSHIPS ====================

    /**
     * Nhiều phòng thuộc 1 Landlord (chủ nhà)
     * FK: rooms.landlord_id → landlord.id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "landlord_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private LandLord landLord;

    /**
     * 1 Phòng có nhiều hợp đồng (theo thời gian)
     */
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Contracts> contracts;

    /**
     * 1 Phòng có nhiều hóa đơn điện nước
     */
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<UtilityBills> utilityBills;

    @OneToOne(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ChatRoom chatRoom;

    @OneToMany(mappedBy = "rooms", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Notifications> notifications;
}
