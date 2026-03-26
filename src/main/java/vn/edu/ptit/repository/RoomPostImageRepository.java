package vn.edu.ptit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.entity.RoomPostImages;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomPostImageRepository extends JpaRepository<RoomPostImages, Long> {

    /**
     * Lấy tất cả ảnh của bài đăng, sắp xếp theo thứ tự hiển thị
     */
    List<RoomPostImages> findByRoomPost_IdOrderByDisplayOrderAsc(Long roomPostId);

    /**
     * Lấy ảnh thumbnail của bài đăng
     */
    Optional<RoomPostImages> findByRoomPost_IdAndThumbnailTrue(Long roomPostId);

    /**
     * Xoá toàn bộ ảnh của bài đăng (dùng khi cập nhật lại ảnh)
     */
    @Modifying
    @Query("DELETE FROM RoomPostImages img WHERE img.roomPost.id = :roomPostId")
    void deleteAllByRoomPostId(@Param("roomPostId") Long roomPostId);

    /**
     * Đếm số ảnh của bài đăng
     */
    long countByRoomPost_Id(Long roomPostId);
}
