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
     * Lấy tất cả yêu cầu gửi đến landlord (mọi status)
     */
    Page<RentalRequests> findByLandLord_IdOrderByCreatedAtDesc(Long landlordId, Pageable pageable);

    /**
     * Lọc yêu cầu theo status cho landlord
     */
    Page<RentalRequests> findByLandLord_IdAndStatusOrderByCreatedAtDesc(
            Long landlordId, RentalRequests.Status status, Pageable pageable);

    /**
     * Đếm yêu cầu PENDING chủ nhà chưa xử lý
     */
    long countByLandLord_IdAndStatus(Long landlordId, RentalRequests.Status status);

    /**
     * Yêu cầu của một bài đăng cụ thể
     */
    Page<RentalRequests> findByRoomPost_IdOrderByCreatedAtDesc(Long roomPostId, Pageable pageable);

    /**
     * Lấy danh sách yêu cầu PENDING của một bài đăng
     */
    List<RentalRequests> findByRoomPost_IdAndStatus(Long roomPostId, RentalRequests.Status status);

    // ==================== PHÍA CUSTOMER ====================

    /**
     * Lấy tất cả yêu cầu của một khách hàng
     */
    Page<RentalRequests> findByCustomer_IdOrderByCreatedAtDesc(Long customerId, Pageable pageable);

    /**
     * Lọc yêu cầu theo status cho customer
     */
    Page<RentalRequests> findByCustomer_IdAndStatusOrderByCreatedAtDesc(
            Long customerId, RentalRequests.Status status, Pageable pageable);

    /**
     * Kiểm tra khách đã có yêu cầu đang active cho bài đăng này chưa
     * (tránh gửi trùng khi status = PENDING hoặc APPROVED)
     */
    boolean existsByRoomPost_IdAndCustomer_IdAndStatusIn(
            Long roomPostId, Long customerId, List<RentalRequests.Status> statuses);

    /**
     * Lấy yêu cầu cụ thể của customer cho một bài đăng (bất kỳ status)
     */
    Optional<RentalRequests> findTopByRoomPost_IdAndCustomer_IdOrderByCreatedAtDesc(
            Long roomPostId, Long customerId);

    // ==================== THỐNG KÊ ====================

    /**
     * Đếm yêu cầu theo trạng thái trong một bài đăng
     */
    long countByRoomPost_IdAndStatus(Long roomPostId, RentalRequests.Status status);

    /**
     * Dashboard landlord: tổng hợp số lượng yêu cầu theo từng status
     */
    @Query("""
            SELECT r.status, COUNT(r)
            FROM RentalRequests r
            WHERE r.landLord.id = :landlordId
            GROUP BY r.status
            """)
    List<Object[]> countGroupByStatusForLandlord(@Param("landlordId") Long landlordId);
}
