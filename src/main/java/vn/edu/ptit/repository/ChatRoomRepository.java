package vn.edu.ptit.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.entity.ChatRoom;
import vn.edu.ptit.entity.Rooms;

import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findById(Long chatRoomId);
    Optional<Rooms> findRoomById(Long chatRoomId);
}
