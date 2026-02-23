package vn.edu.ptit.smart_stay.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@SuperBuilder
@NoArgsConstructor
@Table(name = "utility_bills")
public class UtilityBills implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "contract_id")
    private Long contractId;

    @Column(name = "billing_month", nullable = false)
    private String billingMonth;

    @Column(name = "electricity_old_index", nullable = false)
    private Double electricityOldIndex;

    @Column(name = "electricity_new_index", nullable = false)
    private Double electricityNewIndex;

    @Column(name = "electricity_consumed")
    private Double electricityConsumed;

    @Column(name = "electricity_price_per_kwh")
    private BigDecimal electricityPricePerKwh;

    @Column(name = "electricity_amount")
    private BigDecimal electricityAmount;

    @Column(name = "water_old_index", nullable = false)
    private Double waterOldIndex;

    @Column(name = "water_new_index", nullable = false)
    private Double waterNewIndex;

    @Column(name = "water_consumed")
    private Double waterConsumed;

    @Column(name = "water_price_per_m3")
    private BigDecimal waterPricePerM3;

    @Column(name = "water_amount")
    private BigDecimal waterAmount;

    @Column(name = "internet_fee", nullable = false)
    private BigDecimal internetFee = BigDecimal.ZERO;

    @Column(name = "parking_fee", nullable = false)
    private BigDecimal parkingFee = BigDecimal.ZERO;

    @Column(name = "cleaning_fee", nullable = false)
    private BigDecimal cleaningFee = BigDecimal.ZERO;

    @Column(name = "other_fee", nullable = false)
    private BigDecimal otherFee = BigDecimal.ZERO;

    @Column(name = "other_fee_note")
    private String otherFeeNote;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "status", nullable = false)
    private String status = "'UNPAID'::bill_status";

    @Column(name = "paid_at")
    private LocalDate paidAt;

    @Column(name = "bill_image_url")
    private String billImageUrl;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
