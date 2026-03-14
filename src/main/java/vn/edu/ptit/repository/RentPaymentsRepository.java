package vn.edu.ptit.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.entity.RentPayments;

@Repository
public interface RentPaymentsRepository extends JpaRepository<RentPayments, Long> {
}
