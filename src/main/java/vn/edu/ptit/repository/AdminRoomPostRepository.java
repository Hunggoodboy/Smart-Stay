package vn.edu.ptit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.entity.RoomPosts;

import java.util.List;

@Repository
public interface AdminRoomPostRepository extends JpaRepository<RoomPosts, Long> {

    @Query("""
            SELECT p FROM RoomPosts p
            WHERE (:status IS NULL OR p.status = :status)
              AND (:keyword IS NULL
                   OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(p.address) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(p.city) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY
              CASE
                WHEN p.featured = true
                 AND (p.featuredUntil IS NULL OR p.featuredUntil > CURRENT_TIMESTAMP)
                THEN 1 ELSE 0
              END DESC,
              p.featuredPriority DESC,
              p.createdAt DESC
            """)
    List<RoomPosts> searchForAdmin(
            @Param("status") RoomPosts.Status status,
            @Param("keyword") String keyword
    );
}
