package vn.edu.ptit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.ptit.entity.ChatAiHistory;

import java.util.List;
import java.util.UUID;

public interface ChatAiHistoryRepository extends JpaRepository<ChatAiHistory, UUID> {
    List<ChatAiHistory> findTop30ByConversationIdOrderByCreatedAtDesc(String conversationId);
}
