package vn.edu.ptit.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.entity.UtilityBills;

@Repository
public interface UtilityBillsRepository extends JpaRepository<UtilityBills, Long> {
}
