package vn.edu.ptit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "rent_payments")
public class RentPayments implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "billing_month", nullable = false)
    private String billingMonth;
    @Column(name = "id_card_number")
    private String idCardNumber;
    @Column(name = "rent_amount", nullable = false)
    private Double rentAmount;

    @Column(name = "utility_amount", nullable = false)
    private Double utilityAmount = 0.0;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @Column(name = "late_fee", nullable = false)
    private Double lateFee = 0.0;

    @Column(name = "status", nullable = false)
    private String status = "UNPAID";

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "receipt_url")
    private String receiptUrl;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ==================== RELATIONSHIPS ====================

    /**
     * Nhiều kỳ thanh toán thuộc 1 Hợp đồng
     * FK: rent_payments.contract_id → contracts.id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Contracts contract;

    /**
     * 1 Kỳ thanh toán gộp 1 Hóa đơn điện nước (nullable - có thể chỉ thanh toán tiền thuê)
     * FK: rent_payments.utility_bill_id → utility_bills.id
     * Quan hệ OneToOne vì 1 utility_bill chỉ được gộp vào 1 rent_payment
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utility_bill_id", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UtilityBills utilityBill;
}
