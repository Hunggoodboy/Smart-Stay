package vn.edu.ptit.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.entity.RentPayments;

import java.util.List;
import java.util.Optional;

@Repository
public interface RentPaymentsRepository extends JpaRepository<RentPayments, Long> {

    List<RentPayments> findByContract_Id(Long contractId);

    List<RentPayments> findByStatus(RentPayments.Status status);

    List<RentPayments> findByStatusOrderByDueDateAsc(RentPayments.Status status);

    List<RentPayments> findByContract_IdAndStatus(Long contractId, RentPayments.Status status);

    Optional<RentPayments> findTopByContract_IdAndBillingMonthOrderByCreatedAtDesc(Long contractId, String billingMonth);
}
