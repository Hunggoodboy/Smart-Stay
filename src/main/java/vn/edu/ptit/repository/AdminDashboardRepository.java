package vn.edu.ptit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.entity.RoomPosts;
import vn.edu.ptit.entity.User;

import java.util.List;

@Repository
public interface AdminDashboardRepository extends JpaRepository<User, Long> {

    @Query("""
            SELECT COUNT(u)
            FROM User u
            WHERE u.deletedAt IS NULL
            """)
    long countUsers();

    @Query("""
            SELECT COUNT(u)
            FROM User u
            WHERE u.role = :role
              AND u.deletedAt IS NULL
            """)
    long countUsersByRole(@Param("role") User.Role role);

    @Query("""
            SELECT COUNT(l)
            FROM LandLord l
            WHERE l.verified = false
              AND l.deletedAt IS NULL
            """)
    long countPendingLandlords();

    @Query("""
            SELECT COUNT(l)
            FROM LandLord l
            WHERE l.verified = true
              AND l.deletedAt IS NULL
            """)
    long countVerifiedLandlords();

    @Query("""
            SELECT COUNT(p)
            FROM RoomPosts p
            WHERE p.status IN :statuses
              AND p.deletedAt IS NULL
            """)
    long countPostsNeedReview(@Param("statuses") List<RoomPosts.Status> statuses);

    @Query("""
            SELECT COUNT(p)
            FROM RoomPosts p
            WHERE p.deletedAt IS NULL
            """)
    long countRoomPosts();

    @Query("""
            SELECT COUNT(r)
            FROM Rooms r
            WHERE r.deletedAt IS NULL
            """)
    long countRooms();
}
