package vn.edu.ptit.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.entity.RoomPosts;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomPostRepository extends JpaRepository<RoomPosts, Long> {

    // ==================== TÌM THEO LANDLORD ====================

    /**
     * Lấy tất cả bài đăng của một landlord (mọi status)
     */
    Page<RoomPosts> findByLandLord_IdOrderByCreatedAtDesc(Long landlordId, Pageable pageable);

    /**
     * Lấy bài đăng của landlord theo status cụ thể
     */
    Page<RoomPosts> findByLandLord_IdAndStatusOrderByCreatedAtDesc(
            Long landlordId, RoomPosts.Status status, Pageable pageable);

    /**
     * Đếm bài đăng theo landlord và status
     */
    long countByLandLord_IdAndStatus(Long landlordId, RoomPosts.Status status);

    // ==================== TÌM THEO PHÒNG ====================

    /**
     * Lấy bài đăng ACTIVE hiện tại của một phòng (chỉ nên có 1)
     */
    Optional<RoomPosts> findTopByRoom_IdAndStatusOrderByCreatedAtDesc(
            Long roomId, RoomPosts.Status status);

    /**
     * Lịch sử bài đăng của một phòng
     */
    List<RoomPosts> findByRoom_IdOrderByCreatedAtDesc(Long roomId);

    // ==================== TÌM KIẾM CÔNG KHAI ====================

    /**
     * Tìm kiếm bài đăng ACTIVE có phân trang và lọc
     */
    @Query("""
            SELECT p FROM RoomPosts p
            WHERE p.status = 'ACTIVE'
              AND (:city IS NULL OR LOWER(p.city) LIKE LOWER(CONCAT('%', :city, '%')))
              AND (:district IS NULL OR LOWER(p.district) LIKE LOWER(CONCAT('%', :district, '%')))
              AND (:minPrice IS NULL OR p.postedPrice >= :minPrice)
              AND (:maxPrice IS NULL OR p.postedPrice <= :maxPrice)
              AND (:roomType IS NULL OR p.roomType = :roomType)
              AND (:keyword IS NULL
                    OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(p.address) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY p.publishedAt DESC
            """)
    Page<RoomPosts> searchActive(
            @Param("city") String city,
            @Param("district") String district,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("roomType") String roomType,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * Lấy các bài đăng nổi bật (nhiều lượt xem nhất)
     */
    @Query("SELECT p FROM RoomPosts p WHERE p.status = 'ACTIVE' ORDER BY p.viewCount DESC")
    List<RoomPosts> findTopByViews(Pageable pageable);

    // ==================== CẬP NHẬT THỐNG KÊ ====================

    @Modifying
    @Query("UPDATE RoomPosts p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    void incrementViewCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE RoomPosts p SET p.contactCount = p.contactCount + 1 WHERE p.id = :id")
    void incrementContactCount(@Param("id") Long id);

    /**
     * Đánh dấu bài đăng là đã cho thuê (RENTED) khi hợp đồng được ký
     */
    @Modifying
    @Query("UPDATE RoomPosts p SET p.status = 'RENTED' WHERE p.room.id = :roomId AND p.status = 'ACTIVE'")
    void markRentedByRoomId(@Param("roomId") Long roomId);
}
