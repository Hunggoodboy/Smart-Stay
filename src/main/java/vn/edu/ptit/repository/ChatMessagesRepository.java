package vn.edu.ptit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.entity.ChatMessages;

import java.util.List;

@Repository
public interface ChatMessagesRepository extends JpaRepository<ChatMessages, Long> {
    public List<ChatMessages> findAllBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderBySentAtAsc(Long senderId1, Long receiverId1, Long receiverId2,Long senderId2);
}
