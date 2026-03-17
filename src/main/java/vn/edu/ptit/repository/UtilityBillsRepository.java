package vn.edu.ptit.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.entity.UtilityBills;

import java.util.List;

@Repository
public interface UtilityBillsRepository extends JpaRepository<UtilityBills, Long> {
    UtilityBills findAllById(Long id);
    @Query("SELECT u from UtilityBills u join User us ON u.user.id = us.id where us.id = :userId")
    List<UtilityBills> findAllByUserId(Long userId);
}
