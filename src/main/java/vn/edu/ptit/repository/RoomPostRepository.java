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

    Optional<RoomPosts> findById(Long id);

    // ==================== PHÍA LANDLORD ====================

    /**
     * Tất cả bài đăng của landlord (mọi status), mới nhất trước.
     */
    Page<RoomPosts> findByLandlordIdOrderByCreatedAtDesc(Long landlordId, Pageable pageable);

    /**
     * Bài đăng của landlord lọc theo status.
     */
    Page<RoomPosts> findByLandlordIdAndStatusOrderByCreatedAtDesc(
            Long landlordId, RoomPosts.Status status, Pageable pageable);

    /**
     * Đếm bài đăng đang ACTIVE của landlord.
     */
    long countByLandlordIdAndStatus(Long landlordId, RoomPosts.Status status);

    // ==================== TÌM KIẾM CÔNG KHAI ====================

    /**
     * Tìm kiếm bài đăng ACTIVE với bộ lọc — dùng cho trang khách hàng.
     * Tất cả tham số đều nullable (null = bỏ qua điều kiện đó).
     */
    @Query("""
            SELECT p FROM RoomPosts p
            WHERE p.status = 'ACTIVE'
              AND (:city     IS NULL OR LOWER(p.city)     LIKE LOWER(CONCAT('%', :city,     '%')))
              AND (:district IS NULL OR LOWER(p.district) LIKE LOWER(CONCAT('%', :district, '%')))
              AND (:minPrice IS NULL OR p.monthlyRent >= :minPrice)
              AND (:maxPrice IS NULL OR p.monthlyRent <= :maxPrice)
              AND (:roomType IS NULL OR p.roomType = :roomType)
              AND (:keyword  IS NULL
                    OR LOWER(p.title)   LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(p.address) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY p.createdAt DESC
            """)
    Page<RoomPosts> searchActive(
            @Param("city")      String city,
            @Param("district")  String district,
            @Param("minPrice")  BigDecimal minPrice,
            @Param("maxPrice")  BigDecimal maxPrice,
            @Param("roomType")  String roomType,
            @Param("keyword")   String keyword,
            Pageable pageable
    );

    // ==================== CẬP NHẬT TRẠNG THÁI ====================

    /**
     * Chuyển bài đăng sang RENTED và gán room vừa được tạo.
     * Gọi sau khi Service tạo xong Rooms + Contracts.
     */
    @Modifying
    @Query("UPDATE RoomPosts p SET p.status = 'RENTED', p.room.id = :roomId WHERE p.id = :postId")
    void markRented(@Param("postId") Long postId, @Param("roomId") Long roomId);

    /**
     * Lấy các bài đăng đã hết hạn nhưng chưa được tắt (dùng cho scheduled job).
     */
    @Query("""
            SELECT p FROM RoomPosts p
            WHERE p.status = 'ACTIVE'
              AND p.expiredAt IS NOT NULL
              AND p.expiredAt < CURRENT_TIMESTAMP
            """)
    List<RoomPosts> findExpiredActivePosts();

    @Query("Select r from RoomPosts r where r.landlord.id != :userId")
    List<RoomPosts> findAllRoomsWithoutMine(@Param("userId") Long userId);
}
