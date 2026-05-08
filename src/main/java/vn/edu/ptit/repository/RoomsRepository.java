package vn.edu.ptit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.entity.ChatRoom;
import vn.edu.ptit.entity.Contracts;
import vn.edu.ptit.entity.LandLord;
import vn.edu.ptit.entity.Rooms;

import java.util.Optional;
import java.util.List;

@Repository
public interface RoomsRepository extends JpaRepository<Rooms, Long> {
    Optional<Rooms> findRoomsById(Long id);

    @Query("Select c.landLord from Rooms r join Contracts c on r.id = c.room.id where c.customer.id = :customerId")
    public Optional<LandLord> findLandLordByCustomerId(Long customerId);

    @Query("select r from Rooms r left join fetch r.contract c left join fetch c.customer where r.landLord.id = :landlordId order by r.createdAt desc")
    List<Rooms> findByLandLordIdWithContractAndCustomer(@Param("landlordId") Long landlordId);
}
