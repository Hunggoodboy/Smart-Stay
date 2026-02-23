package vn.edu.ptit.smart_stay.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@Table(name = "rooms")
public class Rooms implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

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
    private BigDecimal internetFee = BigDecimal.ZERO;

    @Column(name = "parking_fee", nullable = false)
    private BigDecimal parkingFee = BigDecimal.ZERO;

    @Column(name = "cleaning_fee", nullable = false)
    private BigDecimal cleaningFee = BigDecimal.ZERO;

    @Column(name = "status", nullable = false)
    private String status = "'AVAILABLE'::room_status";

    @Column(name = "room_type")
    private String roomType;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
