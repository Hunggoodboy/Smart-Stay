package vn.edu.ptit.service.AI;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import vn.edu.ptit.dto.Request.ChatAIRequest;
import vn.edu.ptit.dto.Response.ChatAIResponse;
import vn.edu.ptit.entity.ChatAiHistory;
import vn.edu.ptit.entity.User;
import vn.edu.ptit.repository.ChatAiHistoryRepository;
import vn.edu.ptit.service.Authentication.AuthService;
import org.springframework.ai.chat.messages.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatAiService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final ChatAiHistoryRepository chatAiHistoryRepository;
    private final AuthService  authService;
    public ChatAiService(ChatClient.Builder chatClient,  VectorStore vectorStore, ChatAiHistoryRepository chatAiHistoryRepository, AuthService authService) {
        this.chatClient = chatClient
                .defaultToolNames("createRequestRentalForAi")
                .build();
        this.vectorStore = vectorStore;
        this.chatAiHistoryRepository = chatAiHistoryRepository;
        this.authService = authService;
    }

    public ChatAIResponse generateAnswer(ChatAIRequest chatAIRequest) {
        User currentUser = authService.getUser();
        String thisConversationId = currentUser.getId().toString() + "_" + chatAIRequest.getConversationId();
        String optimizedQuestion = rewriteQuestion(chatAIRequest.getQuestion(), thisConversationId);
        String ragContext = findAnswer(optimizedQuestion);
        String currentQuestionWithRag = "Người dùng hỏi: " + chatAIRequest.getQuestion() +
                "\nThông tin tham khảo để trả lời (nhớ biến link thành thẻ a): \n" + ragContext;
        String answer = chatClient.prompt()
                .messages(getListMessages(thisConversationId, currentQuestionWithRag))
                .call()
                .content();
        ChatAiHistory chatAiHistoryUser = ChatAiHistory.builder()
                .conversationId(thisConversationId)
                .messageType("USER")
                .content(chatAIRequest.getQuestion())
                .user(currentUser)
                .createdAt(java.time.LocalDateTime.now())
                .build();
        ChatAiHistory chatAiHistoryAssistant = ChatAiHistory.builder()
                .conversationId(thisConversationId)
                .messageType("ASSISTANT")
                .content(answer)
                .user(currentUser)
                .createdAt(java.time.LocalDateTime.now())
                .build();
        chatAiHistoryRepository.save(chatAiHistoryUser);
        chatAiHistoryRepository.save(chatAiHistoryAssistant);
        return ChatAIResponse.builder()
                .answer(answer)
                .build();
    }

        private List<Message> getListMessages(String conversationId, String currentQuestionWithRag) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage("Bạn là trợ lý của một trang web cho thuê phòng trọ, dưới đây là lịch sử trò chuyện" +
                "giữa bạn và người dùng, bạn hãy xem lại và trả lời câu hỏi của người dùng thật thông minh và chính xác"+
                ". Bạn sẽ trả lời các câu hỏi của người dùng về việc tìm kiếm phòng trọ," +
                " đăng bài viết, và các vấn đề liên quan đến việc sử dụng trang web. Hãy cung cấp câu trả lời ngắn gọn, chính xác và hữu ích." +
                " Và không dùng markdown. Đặc biệt khi "));
        messages.addAll(addMessageBaseHistory(conversationId));
        messages.add(new UserMessage(currentQuestionWithRag));
        return messages;
    }

    private String rewriteQuestion(String question, String conversationId) {
        List<Message> MessageBaseHistory = addMessageBaseHistory(conversationId);
        if(MessageBaseHistory.isEmpty()) return question;
        List<Message> rewriteMessages = new ArrayList<>();
        String systemPrompt = "Bạn là một bộ máy xử lý ngôn ngữ. Nhiệm vụ của bạn là đọc lịch sử hội thoại" +
                " và viết lại câu hỏi mới nhất của người dùng thành một câu tìm kiếm độc lập (Standalone Query). " +
                "Bao gồm tất cả các từ khóa quan trọng (tên phòng, địa chỉ, giá) được ám chỉ. " +
                "Tuyệt đối KHÔNG trả lời câu hỏi." +
                " Tuyệt đối KHÔNG thêm các từ như 'Dạ', 'Đây là', 'Câu hỏi là'. Chỉ trả về nội dung câu tìm kiếm.";
        rewriteMessages.add(new SystemMessage(systemPrompt));
        rewriteMessages.addAll(MessageBaseHistory);
        rewriteMessages.add(new UserMessage("Câu hỏi cần viết lại: " + question));
        String rewrittenQuery = chatClient.prompt()
                                          .messages(rewriteMessages)
                                          .call()
                                          .content();
        System.out.println("=== CÂU HỎI GỐC: " + question);
        System.out.println("=== CÂU ĐÃ ĐƯỢC AI DỊCH: " + rewrittenQuery);

        return rewrittenQuery;
    }

    private List<Message> addMessageBaseHistory(String conversationId){
        List<Message> messages = new ArrayList<>();
        List<ChatAiHistory> chatAiHistories = chatAiHistoryRepository.findTop30ByConversationIdOrderByCreatedAtDesc(conversationId);
        Collections.reverse(chatAiHistories);
        chatAiHistories.forEach(history -> {
            if(history.getMessageType().equals("USER")) {
                messages.add(new UserMessage(history.getContent()));
            }
            else{
                messages.add(new AssistantMessage(history.getContent()));
            }});
        return messages;
    }

    private String findAnswer(String question) {
        List<Document> similarDocuments = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(4)
                        .similarityThreshold(0.5)
                        .build()
        );
        String answer = similarDocuments.stream().map(docs -> {
            String roomId = docs.getMetadata().get("roomId").toString();
            String content = docs.getText();
            String url = "\nLink: http://localhost:8081/room-detail?id=" + roomId;
            return content + url;
        }).collect(Collectors.joining("\n"));
        return answer;
    }
}
