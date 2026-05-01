package vn.edu.ptit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.entity.Contracts;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractsRepository extends JpaRepository<Contracts, Long> {

    @Query("SELECT distinct c FROM Contracts c WHERE c.landLord.id = :userId OR c.customer.id = :userId ORDER BY c.createdAt DESC")
    List<Contracts> findContractsByUserIdOrderByCreatedAt(@Param("userId") Long userId);
    @Query("select c from Contracts c where c.room.id = :roomId")
    Optional<Contracts> findContractsByRoomId(@Param("roomId") Long roomId);
    Optional<Contracts> findByRoomIdAndStatus(Long roomId, String status);
    Optional<Contracts> findByContractCode(@Param("contractCode") String contractCode);
}
