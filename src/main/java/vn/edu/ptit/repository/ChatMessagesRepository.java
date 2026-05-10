package vn.edu.ptit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.ptit.dto.Response.ChatMessagesSummaryResponse;
import vn.edu.ptit.entity.ChatMessages;

import java.util.List;

@Repository
public interface ChatMessagesRepository extends JpaRepository<ChatMessages, Long> {
    public List<ChatMessages> findAllBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderBySentAtAsc(Long senderId1, Long receiverId1, Long receiverId2,Long senderId2);
    @Query("""
    SELECT m FROM ChatMessages m
    WHERE m.id IN (
        SELECT MAX(m2.id) FROM ChatMessages m2
        WHERE m2.sender.id = :userId OR m2.receiver.id = :userId
        GROUP BY CASE
            WHEN m2.sender.id = :userId THEN m2.receiver.id
            ELSE m2.sender.id
        END
    )
    ORDER BY m.sentAt DESC
""")

    List<ChatMessages> findLatestMessagePerConversation(@Param("userId") Long userId);

    @Query("SELECT COUNT(m) FROM ChatMessages m WHERE m.sender.id = :senderId AND m.receiver.id = :receiverId AND m.isRead = false")
    int countUnread(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);

    @Query("select new vn.edu.ptit.dto.Response.ChatMessagesSummaryResponse(case when m.receiver.id = :userId then m.sender.id else m.receiver.id end ," +
            " case when m.receiver.id = :userId then m.sender.fullName else m.receiver.fullName end , m.content, m.createdAt)" +
            "  From ChatMessages m where m.id in " +
            " (Select max(m2.id) from ChatMessages m2 where m2.sender.id = :userId or m2.receiver.id = :userId group by" +
            " case when m2.receiver.id = :userId then m2.sender.id else m2.receiver.id END ) "+
            " order by m.createdAt desc")
    List<ChatMessagesSummaryResponse> findInboxSummary(@Param("userId") Long userId);
}