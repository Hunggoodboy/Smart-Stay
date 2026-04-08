package vn.edu.ptit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.entity.Contracts;

import java.util.Optional;

@Repository
public interface ContractsRepository extends JpaRepository<Contracts, Long> {
    @Query("select c from Contracts c where c.customer.id = :userId")
    Optional<Contracts> findContractsByUserId(@Param("userId") Long userId);
    @Query("select c from Contracts c where c.room.id = :roomId")
    Optional<Contracts> findContractsByRoomId(@Param("roomId") Long roomId);
    Optional<Contracts> findByRoomIdAndStatus(Long roomId, String status);
}
