package vn.edu.ptit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "utility_bills")
public class UtilityBills implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "billing_month", nullable = false)
    private Long billingMonth;

    @Column(name = "electricity_old_index", nullable = false)
    private Double electricityOldIndex = 0.0;

    @Column(name = "electricity_new_index", nullable = false)
    private Double electricityNewIndex = 0.0;

    @Column(name = "electricity_consumed")
    private Double electricityConsumed;

    @Column(name = "electricity_price_per_kwh")
    private BigDecimal electricityPricePerKwh;

    @Column(name = "electricity_amount")
    private BigDecimal electricityAmount;

    @Column(name = "water_old_index", nullable = false)
    private Double waterOldIndex = 0.0;

    @Column(name = "water_new_index", nullable = false)
    private Double waterNewIndex = 0.0;

    @Column(name = "water_consumed")
    private Double waterConsumed;

    @Column(name = "water_price_per_m3")
    private Double waterPricePerM3;

    @Column(name = "water_amount")
    private BigDecimal waterAmount;

    @Column(name = "internet_fee", nullable = false)
    private Double internetFee = 0.0;

    @Column(name = "parking_fee", nullable = false)
    private Double parkingFee = 0.0;

    @Column(name = "cleaning_fee", nullable = false)
    private Double cleaningFee = 0.0;

    @Column(name = "other_fee", nullable = false)
    private Double otherFee = 0.0;

    @Column(name = "other_fee_note")
    private String otherFeeNote;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    public enum Status{
        UNPAID,
        PAID
    }

    @Column(name = "status", nullable = false)
    private Status status = Status.UNPAID;

    @Column(name = "paid_at")
    private LocalDate paidAt;


    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ==================== RELATIONSHIPS ====================

    /**
     * Nhiều hóa đơn thuộc 1 Phòng
     * FK: utility_bills.room_id → rooms.id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Rooms room;

    /**
     * Nhiều hóa đơn thuộc 1 Hợp đồng (nullable - có thể tạo trước khi có hợp đồng)
     * FK: utility_bills.contract_id → contracts.id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Contracts contract;

    /**
     * 1 Hóa đơn điện nước được gộp vào 1 kỳ thanh toán (nullable)
     */
    @OneToOne(mappedBy = "utilityBill", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private RentPayments rentPayment;

    //Toi user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;
}
