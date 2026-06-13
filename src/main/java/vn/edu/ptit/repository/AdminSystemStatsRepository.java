package vn.edu.ptit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.entity.User;

import vn.edu.ptit.entity.RentPayments;
import java.util.List;

@Repository
public interface AdminSystemStatsRepository extends JpaRepository<User, Long> {

    /**
     * Đếm user đăng ký mới theo từng tháng trong năm
     * Trả về: [month (int), count (long)]
     */
    @Query("""
            SELECT EXTRACT(MONTH FROM u.createdAt), COUNT(u)
            FROM User u
            WHERE EXTRACT(YEAR FROM u.createdAt) = :year
              AND u.deletedAt IS NULL
            GROUP BY EXTRACT(MONTH FROM u.createdAt)
            ORDER BY EXTRACT(MONTH FROM u.createdAt)
            """)
    List<Object[]> countNewUsersByMonth(@Param("year") int year);

    /**
     * Đếm phòng được thuê (contract tạo mới) theo từng tháng trong năm
     * Trả về: [month (int), count (long)]
     */
    @Query("""
            SELECT EXTRACT(MONTH FROM c.createdAt), COUNT(c)
            FROM Contracts c
            WHERE EXTRACT(YEAR FROM c.createdAt) = :year
            GROUP BY EXTRACT(MONTH FROM c.createdAt)
            ORDER BY EXTRACT(MONTH FROM c.createdAt)
            """)
    List<Object[]> countRoomsRentedByMonth(@Param("year") int year);

    /**
     * Tổng doanh thu (rent_payments đã PAID) theo từng tháng trong năm
     * Trả về: [month (String từ billingMonth), totalAmount (double)]
     */
    @Query("""
            SELECT SUBSTRING(p.billingMonth, 6, 2), COALESCE(SUM(p.totalAmount), 0.0)
            FROM RentPayments p
            WHERE p.billingMonth LIKE CONCAT(:yearText, '%')
              AND p.status = :status
            GROUP BY SUBSTRING(p.billingMonth, 6, 2)
            ORDER BY SUBSTRING(p.billingMonth, 6, 2)
            """)
    List<Object[]> revenueByMonth(@Param("yearText") String yearText, @Param("status") RentPayments.Status status);

    /**
     * Lấy danh sách user mới nhất (không bị xóa)
     */
    @Query("""
            SELECT u
            FROM User u
            WHERE u.deletedAt IS NULL
            ORDER BY u.createdAt DESC
            """)
    List<User> findRecentUsers();
}
