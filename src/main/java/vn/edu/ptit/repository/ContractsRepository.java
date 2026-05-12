package vn.edu.ptit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.entity.Contracts;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractsRepository extends JpaRepository<Contracts, Long> {

    @Query("Update Contracts c Set c.isExpiration = false where c.endDate < CURRENT_DATE and c.isExpiration = true")
    boolean updateExpiredContracts();

    @Query("SELECT distinct c FROM Contracts c WHERE (c.landLord.id = :userId OR c.customer.id = :userId) AND c.isExpiration = true ORDER BY c.createdAt DESC")
    List<Contracts> findValidContractsForUser(@Param("userId") Long userId);
    @Query("SELECT distinct c FROM Contracts c WHERE (c.landLord.id = :userId OR c.customer.id = :userId) AND c.isExpiration = false ORDER BY c.createdAt DESC")
    List<Contracts> findExpiredContractsForUser(@Param("userId") Long userId);
    /** Tìm Contract từ phía Rooms (Rooms giữ FK contract_id) */
    @Query("select r.contract from Rooms r where r.id = :roomId and r.contract is not null")
    Optional<Contracts> findContractsByRoomId(@Param("roomId") Long roomId);

    /** Tìm Contract theo id */
    Optional<Contracts> findByRentalRequestId(@Param("rentalRequestId") Long rentalRequestId);
    Optional<Contracts> findByContractCode(@Param("contractCode") String contractCode);
    Optional<Contracts> findByCustomerIdAndLandLordId(@Param("cutomerId") Long customerId, @Param("landlordId") Long landlordId);
}
