package vn.edu.ptit.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.entity.ChatRoom;
import vn.edu.ptit.entity.Contracts;
import vn.edu.ptit.entity.LandLord;
import vn.edu.ptit.entity.Rooms;
import vn.edu.ptit.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomsRepository extends JpaRepository<Rooms, Long> {
    Optional<Rooms> findRoomsById(Long id);

    /** T\u00ecm LandLord c\u1ee7a ph\u00f2ng m\u00e0 customer \u0111ang thu\u00ea (qua contract n\u1eb1m trong Rooms) */
    @Query("Select r.landLord from Rooms r where r.contract.customer.id = :customerId and r.deletedAt is null")
    Optional<User> findLandLordByCustomerId(Long customerId);

    /** Tìm Room theo contractId (FK contract_id nằm ở Rooms) */
    Optional<Rooms> findByContractId(Long contractId);

    /** Kiểm tra RoomPost đã có phòng đang RENTED chưa — dùng để chặn yêu cầu thuê mới */
    boolean existsByRoomPostIdAndStatus(Long roomPostId, Rooms.Status status);

    /** Tìm Room theo roomPostId */
    Optional<Rooms> findByRoomPostId(Long roomPostId);

    @Query("select r from Rooms r where r.landLord.id = :landLordId and r.deletedAt != null ")
    List<Rooms> findRoomsIsDeletedByLandLordId(Long landLordId);

    List<Rooms> findRoomsByLandLordIdAndDeletedAt(Long id, LocalDateTime deletedAt);
}
