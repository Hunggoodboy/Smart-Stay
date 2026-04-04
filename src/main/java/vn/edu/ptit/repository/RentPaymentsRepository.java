package vn.edu.ptit.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.edu.ptit.entity.RentPayments;

@Repository
public interface RentPaymentsRepository extends JpaRepository<RentPayments, Long> {

    List<RentPayments> findByContract_Id(Long contractId);

    List<RentPayments> findByStatus(String status);

    List<RentPayments> findByStatusOrderByDueDateAsc(String status);

    List<RentPayments> findByContract_IdAndStatus(Long contractId, String status);

    Optional<RentPayments> findTopByContract_IdAndBillingMonthOrderByCreatedAtDesc(Long contractId, String billingMonth);
}