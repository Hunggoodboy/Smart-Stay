package vn.edu.ptit.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import vn.edu.ptit.entity.UtilityBills;

@Repository
public interface UtilityBillsRepository extends JpaRepository<UtilityBills, Long> {

    List<UtilityBills> findByRoom_Id(Long roomId);

    List<UtilityBills> findByStatus(String status);

    List<UtilityBills> findByRoom_IdOrderByCreatedAtDesc(Long roomId);
//    @Query()
//    List<UtilityBills> findByRoom_IdOrderByCreatedAtAsc(Long roomId);
    Optional<UtilityBills> findTopByRoom_IdAndBillingMonthOrderByCreatedAtDesc(Long roomId, String billingMonth);
}