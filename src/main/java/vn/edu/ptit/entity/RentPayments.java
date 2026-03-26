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

    /**
     * Tháng thanh toán, định dạng "YYYY-MM" (vd: "2025-03")
     * Thống nhất kiểu String với UtilityBills
     */
    @Column(name = "billing_month", nullable = false, length = 7)
    private String billingMonth;

    @Column(name = "id_card_number")
    private String idCardNumber;

    @Column(name = "rent_amount", nullable = false)
    private Double rentAmount;

    /**
     * Tổng tiền dịch vụ (lấy từ UtilityBills.totalAmount nếu có gộp)
     */
    @Column(name = "utility_amount", nullable = false)
    private Double utilityAmount = 0.0;

    /**
     * Tổng phải thu = rentAmount + utilityAmount + lateFee
     */
    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @Column(name = "late_fee", nullable = false)
    private Double lateFee = 0.0;

    public enum Status {
        UNPAID,
        PAID,
        PARTIALLY_PAID,
        OVERDUE
    }

    /**
     * Đổi từ String sang Enum để tránh lỗi typo và dễ query
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private Status status = Status.UNPAID;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "receipt_url")
    private String receiptUrl;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at", updatable = false)
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
     * Thêm quan hệ trực tiếp đến Phòng để tránh phải đi vòng qua contract
     * FK: rent_payments.room_id → rooms.id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Rooms room;

    /**
     * 1 Kỳ thanh toán có thể gộp 1 Hóa đơn điện nước (nullable)
     * FK: rent_payments.utility_bill_id → utility_bills.id
     * OneToOne vì 1 utility_bill chỉ được gộp vào đúng 1 rent_payment
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utility_bill_id", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private UtilityBills utilityBill;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Customer customer;
}