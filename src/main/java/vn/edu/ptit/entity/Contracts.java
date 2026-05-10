package vn.edu.ptit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "contracts")
public class Contracts implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_code", nullable = false, unique = true)
    private String contractCode;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;


    @Column(name = "monthly_rent", nullable = false)
    private Double monthlyRent;

    @Column(name = "deposit_amount")
    private Double depositAmount;

    @Column(name = "billing_date", nullable = false)
    private Long billingDate = 5L;

    @Column(name = "status", nullable = false)
    private String status = "PENDING";

    @Column(name = "contract_file_url")
    private String contractFileUrl;


    @Column(name = "num_occupants", nullable = false)
    private Long numOccupants = 1L;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "electricity_price_per_kwh")
    private Double electricityPricePerKwh;

    @Column(name = "water_price_per_m3")
    private Double waterPricePerM3;

    @Column(name = "internet_fee", nullable = false)
    private Double internetFee = 0.0;

    @Column(name = "parking_fee", nullable = false)
    private Double parkingFee = 0.0;

    @Column(name = "cleaning_fee", nullable = false)
    private Double cleaningFee = 0.0;

    // ==================== RELATIONSHIPS ====================

    /**
     * Nhiều hợp đồng thuộc 1 LandLord (người thuê)
     * FK: contracts.landlord_id ->
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "landlord_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User landLord;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User customer;

    /**
     * Nhiều hợp đồng thuộc 1 Phòng
     * FK: contracts.room_id → rooms.id
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    @ToString.Exclude
    private Rooms room;

    /**
     * 1 Hợp đồng phát sinh nhiều kỳ thanh toán
     */
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<RentPayments> rentPayments;

    /**
     * 1 Hợp đồng phát sinh nhiều hóa đơn điện nước
     */
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<UtilityBills> utilityBills;
}
