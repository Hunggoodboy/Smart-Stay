package vn.edu.ptit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@Table(name = "utility_bills")
@NoArgsConstructor
@AllArgsConstructor
public class UtilityBills implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tháng lập hóa đơn, định dạng "YYYY-MM" (vd: "2025-03")
     * Thống nhất kiểu String với RentPayments
     */
    @Column(name = "billing_month", nullable = false, length = 7)
    private String billingMonth;

    // ==================== ĐIỆN ====================

    @Column(name = "electricity_old_index", nullable = false)
    private Double electricityOldIndex = 0.0;

    @Column(name = "electricity_new_index", nullable = false)
    private Double electricityNewIndex = 0.0;

    @Column(name = "electricity_consumed")
    private Double electricityConsumed;

    @Column(name = "electricity_price_per_kwh")
    private Double electricityPricePerKwh;

    @Column(name = "electricity_amount")
    private Double electricityAmount;

    // ==================== NƯỚC ====================

    @Column(name = "water_old_index", nullable = false)
    private Double waterOldIndex = 0.0;

    @Column(name = "water_new_index", nullable = false)
    private Double waterNewIndex = 0.0;

    @Column(name = "water_consumed")
    private Double waterConsumed;

    @Column(name = "water_price_per_m3")
    private Double waterPricePerM3;

    @Column(name = "water_amount")
    private Double waterAmount;

    // ==================== PHÍ DỊCH VỤ ====================

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

    // ==================== TỔNG & TRẠNG THÁI ====================

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    /**
     * Đổi sang LocalDate — hạn thanh toán chỉ cần ngày, không cần giờ/phút/giây
     */
    @Column(name = "due_date")
    private LocalDate dueDate;

    public enum Status {
        UNPAID,
        PAID
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private Status status = Status.UNPAID;

    @Column(name = "paid_at")
    private LocalDate paidDate;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at", updatable = false)
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
     * Hóa đơn điện nước thuộc 1 Hợp đồng (nullable)
     * FK: utility_bills.contract_id → contracts.id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Contracts contract;

    /**
     * Hóa đơn điện nước do LandLord tạo, KHÔNG phải User chung
     * FK: utility_bills.landlord_id → landlord.id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "landlord_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private LandLord landLord;

    /**
     * 1 Hóa đơn điện nước có thể được gộp vào 1 kỳ thanh toán (nullable)
     * mappedBy phía RentPayments giữ FK
     */
    @OneToOne(mappedBy = "utilityBill", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private RentPayments rentPayment;
}