package vn.edu.ptit.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.entity.ChatRoom;
import vn.edu.ptit.entity.Contracts;
import vn.edu.ptit.entity.LandLord;
import vn.edu.ptit.entity.Rooms;
import vn.edu.ptit.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomsRepository extends JpaRepository<Rooms, Long> {
    Optional<Rooms> findRoomsById(Long id);

    @Query("Select c.landLord from Rooms r join Contracts c on r.id = c.room.id where c.customer.id = :customerId")
    public Optional<User> findLandLordByCustomerId(Long customerId);

    List<Rooms> findRoomsByLandLordId(Long id);
}
