package vn.edu.ptit.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.entity.UtilityBills;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtilityBillsRepository extends JpaRepository<UtilityBills, Long> {
    UtilityBills findAllById(Long id);

    List<UtilityBills> findByRoom_Id(Long roomId);

    List<UtilityBills> findByStatus(String status);

    List<UtilityBills> findByRoom_IdOrderByCreatedAtDesc(Long roomId);
//    @Query()
//    List<UtilityBills> findByRoom_IdOrderByCreatedAtAsc(Long roomId);
    Optional<UtilityBills> findTopByRoom_IdAndBillingMonthOrderByCreatedAtDesc(Long roomId, String billingMonth);
    @Query("""
            SELECT u
            FROM UtilityBills u
            where u.contract.customer.id = :userId
            ORDER BY u.billingMonth DESC, u.createdAt DESC
            """)
    List<UtilityBills> findAllByUserId(@Param(value = "userId") Long userId);
}