package vn.edu.ptit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Người cho thuê nhà — kế thừa từ User
 * Bảng riêng: landlords (JOINED strategy)
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Table(name = "landlords")
@NoArgsConstructor
@DiscriminatorValue("LANDLORD")
@PrimaryKeyJoinColumn(name = "user_id")
public class LandLord extends User {

    private static final long serialVersionUID = 1L;

    @Column(name = "id_card_number", unique = true)
    private String idCardNumber;

    @Column(name = "address")
    private String address;

    @Column(name = "is_verified", nullable = false)
    private Boolean verified = false;

    // ==================== RELATIONSHIPS ====================

    /**
     * 1 Landlord đăng nhiều phòng cho thuê
     */
    @OneToMany(mappedBy = "landLord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Rooms> rooms;

    /**
     * 1 Landlord có nhiều hợp đồng cho thuê
     */
    @OneToMany(mappedBy = "landLord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Contracts> contracts;

    /**
     * 1 Landlord tham gia nhiều phòng chat
     */
    @OneToMany(mappedBy = "landLord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ChatRoom> chatRooms;

    @OneToMany(mappedBy = "landlord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Notifications> notifications;

    @OneToMany(mappedBy = "landlord", cascade = CascadeType.ALL)
    private List<RentalRequests>  rentalRequests;

    @OneToMany(mappedBy = "landlord", cascade = CascadeType.ALL)
    private List<RoomPosts>  roomPosts;
}