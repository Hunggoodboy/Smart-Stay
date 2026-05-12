package vn.edu.ptit.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.ptit.entity.RentalRequests;

import java.util.List;
import java.util.Optional;

@Repository
public interface RentalRequestRepository extends JpaRepository<RentalRequests, Long> {
    @Query("SELECT r, r.roomPost.mainImageUrl, r.customer.id" +
            " FROM RentalRequests r " +
            "where (r.landlord.id = :userId or r.customer.id = :userId) and r.deletedAt is null " +
            "order by r.createdAt desc")
    List<Object[]> findAllWithRoomPostAndCustomer(@Param("userId") Long userId);

    boolean existsByRoomPostIdAndCustomerId(Long roomId, Long customerId);

    Optional<RentalRequests> findByRoomPostIdAndCustomerId(@Param("roomId") Long roomId, @Param("customerId") Long customerId);

}
