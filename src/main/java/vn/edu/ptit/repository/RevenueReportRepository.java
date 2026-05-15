package vn.edu.ptit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.entity.RentPayments;

import java.util.List;

@Repository
public interface RevenueReportRepository extends JpaRepository<RentPayments, Long> {

    @Query("SELECT COALESCE(SUM(p.rentAmount), 0.0), COALESCE(SUM(p.utilityAmount), 0.0), " +
            "COALESCE(SUM(p.totalAmount), 0.0), COUNT(p) " +
            "FROM RentPayments p " +
            "WHERE p.billingMonth = :billingMonth " +
            "AND p.status = :paidStatus " +
            "AND (:landlordId IS NULL OR p.contract.landLord.id = :landlordId)")
    List<Object[]> reportByMonth(
            @Param("billingMonth") String billingMonth,
            @Param("landlordId") Long landlordId,
            @Param("paidStatus") RentPayments.Status paidStatus
    );

    @Query("SELECT COALESCE(SUM(p.rentAmount), 0.0), COALESCE(SUM(p.utilityAmount), 0.0), " +
            "COALESCE(SUM(p.totalAmount), 0.0), COUNT(p) " +
            "FROM RentPayments p " +
            "WHERE p.billingMonth LIKE CONCAT(:yearText, '%') " +
            "AND p.status = :paidStatus " +
            "AND (:landlordId IS NULL OR p.contract.landLord.id = :landlordId)")
    List<Object[]> reportByYear(
            @Param("yearText") String yearText,
            @Param("landlordId") Long landlordId,
            @Param("paidStatus") RentPayments.Status paidStatus
    );
}
