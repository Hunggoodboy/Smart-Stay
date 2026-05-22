package vn.edu.ptit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.entity.Rooms;

import java.util.List;

@Repository
public interface AdminRoomRepository extends JpaRepository<Rooms, Long> {

    @Query("""
            SELECT r FROM Rooms r
            WHERE (:status IS NULL OR r.status = :status)
              AND (:keyword IS NULL
                   OR LOWER(r.roomNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(r.address) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(r.city) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY r.createdAt DESC
            """)
    List<Rooms> searchForAdmin(
            @Param("status") Rooms.Status status,
            @Param("keyword") String keyword
    );
}
