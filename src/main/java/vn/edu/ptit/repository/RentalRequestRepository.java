package vn.edu.ptit.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.entity.RentalRequests;

import java.util.List;
import java.util.Optional;

@Repository
public interface RentalRequestRepository extends JpaRepository<RentalRequests, Long> {

    // ==================== PHÍA LANDLORD ====================

    /**
     * Tất cả yêu cầu gửi đến landlord, mới nhất trước.
     */
    Page<RentalRequests> findByLandLord_IdOrderByCreatedAtDesc(Long landlordId, Pageable pageable);

    /**
     * Lọc yêu cầu theo status cho landlord.
     */
    Page<RentalRequests> findByLandLord_IdAndStatusOrderByCreatedAtDesc(
            Long landlordId, RentalRequests.Status status, Pageable pageable);

    /**
     * Đếm yêu cầu PENDING chủ nhà chưa xử lý — dùng hiển thị badge thông báo.
     */
    long countByLandLord_IdAndStatus(Long landlordId, RentalRequests.Status status);

    /**
     * Lấy tất cả yêu cầu PENDING của một bài đăng cụ thể.
     * Dùng để tự động CANCELLED các yêu cầu còn lại khi landlord đã duyệt 1 yêu cầu.
     */
    List<RentalRequests> findByRoomPost_IdAndStatus(Long roomPostId, RentalRequests.Status status);

    // ==================== PHÍA CUSTOMER ====================

    /**
     * Tất cả yêu cầu của một khách hàng, mới nhất trước.
     */
    Page<RentalRequests> findByCustomer_IdOrderByCreatedAtDesc(Long customerId, Pageable pageable);

    /**
     * Lọc yêu cầu theo status cho customer.
     */
    Page<RentalRequests> findByCustomer_IdAndStatusOrderByCreatedAtDesc(
            Long customerId, RentalRequests.Status status, Pageable pageable);

    /**
     * Kiểm tra customer đã có yêu cầu đang active cho bài đăng này chưa.
     * Ngăn gửi trùng khi status = PENDING hoặc APPROVED.
     */
    boolean existsByRoomPost_IdAndCustomer_IdAndStatusIn(
            Long roomPostId, Long customerId, List<RentalRequests.Status> statuses);

    /**
     * Lấy yêu cầu gần nhất của customer cho một bài đăng (bất kỳ status).
     */
    Optional<RentalRequests> findTopByRoomPost_IdAndCustomer_IdOrderByCreatedAtDesc(
            Long roomPostId, Long customerId);

    // ==================== THỐNG KÊ LANDLORD ====================

    /**
     * Tổng hợp số lượng yêu cầu theo từng status cho landlord — dùng cho dashboard.
     * Trả về List<Object[]> với [status, count].
     */
    @Query("""
            SELECT r.status, COUNT(r)
            FROM RentalRequests r
            WHERE r.landLord.id = :landlordId
            GROUP BY r.status
            """)
    List<Object[]> countGroupByStatusForLandlord(@Param("landlordId") Long landlordId);
}
