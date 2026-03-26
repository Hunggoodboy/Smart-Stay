package vn.edu.ptit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Người thuê nhà — kế thừa từ User
 * Bảng riêng: customers (JOINED strategy)
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Table(name = "customers")
@DiscriminatorValue("TENANT")
@PrimaryKeyJoinColumn(name = "user_id")
public class Customer extends User {

    private static final long serialVersionUID = 1L;

    @Column(name = "id_card_number", unique = true)
    private String idCardNumber;

    @Column(name = "address")
    private String address;

    @Column(name = "is_verified", nullable = false)
    private Boolean verified = false;

    // ==================== RELATIONSHIPS ====================

    /**
     * 1 Customer ký nhiều hợp đồng thuê phòng
     */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Contracts> contracts;

    /**
     * 1 Customer tham gia nhiều phòng chat
     */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ChatRoom> chatRooms;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<RentPayments> rentPayments;
}