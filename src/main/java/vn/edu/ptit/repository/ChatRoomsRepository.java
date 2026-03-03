package vn.edu.ptit.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.entity.ChatRooms;

@Repository
public interface ChatRoomsRepository extends JpaRepository<ChatRooms, Long> {
}
