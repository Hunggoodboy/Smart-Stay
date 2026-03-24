package vn.edu.ptit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.entity.UtilityBills;

import java.util.List;

@Repository
public interface UtilityBillsRepository extends JpaRepository<UtilityBills, Long> {
    UtilityBills findAllById(Long id);

    @Query("""
            SELECT u
            FROM UtilityBills u
            LEFT JOIN u.contract c
            LEFT JOIN c.customer customer
            WHERE (u.user IS NOT NULL AND u.user.id = :userId)
               OR (customer IS NOT NULL AND customer.id = :userId)
            ORDER BY u.billingMonth DESC, u.createdAt DESC
            """)
    List<UtilityBills> findAllByUserId(Long userId);
}
