package vn.edu.ptit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.entity.ChatMessages;

@Repository
public interface ChatMessagesRepository extends JpaRepository<ChatMessages, Long> {
}
