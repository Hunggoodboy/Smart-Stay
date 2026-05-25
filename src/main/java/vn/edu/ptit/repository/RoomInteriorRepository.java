package vn.edu.ptit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.entity.RoomInterior;
import java.util.List;

@Repository
public interface RoomInteriorRepository extends JpaRepository<RoomInterior, Long> {
    List<RoomInterior> findByRoomPostId(Long roomPostId);
}
