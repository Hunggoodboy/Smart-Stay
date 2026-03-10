package vn.edu.ptit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
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

    @Column(name = "actual_end_date")
    private LocalDate actualEndDate;

    @Column(name = "monthly_rent", nullable = false)
    private Double monthlyRent;

    @Column(name = "deposit_amount")
    private Double depositAmount;

    @Column(name = "deposit_returned", nullable = false)
    private Boolean depositReturned = Boolean.FALSE;

    @Column(name = "billing_date", nullable = false)
    private Long billingDate = 5L;

    @Column(name = "status", nullable = false)
    private String status = "PENDING";

    @Column(name = "contract_file_url")
    private String contractFileUrl;

    @Column(name = "termination_reason")
    private String terminationReason;

    @Column(name = "num_occupants", nullable = false)
    private Long numOccupants = 1L;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ==================== RELATIONSHIPS ====================

    /**
     * Nhiều hợp đồng thuộc 1 User (người thuê)
     * FK: contracts.user_id → users.id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    /**
     * Nhiều hợp đồng thuộc 1 Customer (chủ nhà)
     * FK: contracts.customer_id → customers.id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Customers customer;

    /**
     * Nhiều hợp đồng thuộc 1 Phòng
     * FK: contracts.room_id → rooms.id
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
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
