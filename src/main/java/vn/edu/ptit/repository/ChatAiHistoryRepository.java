package vn.edu.ptit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.ptit.dto.Response.ChatAiHistoryResponse;
import vn.edu.ptit.entity.ChatAiHistory;

import java.util.List;
import java.util.UUID;

public interface ChatAiHistoryRepository extends JpaRepository<ChatAiHistory, UUID> {
    List<ChatAiHistory> findTop30ByConversationIdOrderByCreatedAtDesc(String conversationId);
    List<ChatAiHistory> findByConversationIdOrderByCreatedAtDesc(String conversationId);
    @Query(value = "select distinct on (conversation_id) * " +
            "from chat_ai_history " +
            "where user_id = :userId " +
            "order by conversation_id, created_at desc;", nativeQuery = true)
    List<ChatAiHistory> getChatAiHistoryByUserId(@Param("userId") Long userId);
}
