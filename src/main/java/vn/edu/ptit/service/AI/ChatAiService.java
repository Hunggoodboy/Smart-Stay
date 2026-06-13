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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatAiService {

    @Value("${app.ai.primary-model:gemini-2.5-flash}")
    private String primaryModel;

    @Value("${app.ai.fallback-model:gemini-3.1-flash-lite}")
    private String fallbackModel;

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final ChatAiHistoryRepository chatAiHistoryRepository;
    private final AuthService  authService;
    private final RetryTemplate retryTemplate;

    public ChatAiService(ChatClient.Builder chatClient,  VectorStore vectorStore, ChatAiHistoryRepository chatAiHistoryRepository, AuthService authService) {
        this.chatClient = chatClient
                .defaultToolNames("createRequestRentalForAi", "scheduleAppointmentForAi","getRentalRequestsForAi", "getTodayRentalRequestsForAi", "approveRentalRequestForAi")
                .build();
        this.vectorStore = vectorStore;
        this.chatAiHistoryRepository = chatAiHistoryRepository;
        this.authService = authService;
        
        // Cấu hình RetryTemplate tùy chỉnh để tự động chờ khi bị lỗi 429
        this.retryTemplate = new RetryTemplate();
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(25000); // Bắt đầu chờ 25 giây
        backOffPolicy.setMultiplier(2.0); // Tăng gấp đôi nếu tiếp tục lỗi
        backOffPolicy.setMaxInterval(120000); // Chờ tối đa 2 phút
        this.retryTemplate.setBackOffPolicy(backOffPolicy);
        
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(3, Collections.singletonMap(Exception.class, true)) {
            @Override
            public boolean canRetry(org.springframework.retry.RetryContext context) {
                Throwable t = context.getLastThrowable();
                if (t != null && !super.canRetry(context)) {
                    return false;
                }
                if (t != null) {
                    String msg = t.getMessage() != null ? t.getMessage() : "";
                    if (t instanceof NonTransientAiException || msg.contains("429")) {
                        System.err.println("Nhận diện lỗi AI Rate Limit (429/NonTransientAiException). Chuẩn bị Retry...");
                        return true;
                    }
                    if (t instanceof TransientAiException) {
                        return true;
                    }
                    // Nếu lỗi khác thì không retry
                    return false;
                }
                return true;
            }
        };
        this.retryTemplate.setRetryPolicy(retryPolicy);
    }

    public ChatAIResponse generateAnswer(ChatAIRequest chatAIRequest) {
        User currentUser = authService.getUser();
        String thisConversationId = currentUser.getId().toString() + "_" + chatAIRequest.getConversationId();
        String optimizedQuestion = rewriteQuestion(chatAIRequest.getQuestion(), thisConversationId);
        String ragContext = findAnswer(optimizedQuestion);
        String currentQuestionWithRag = "Người dùng hỏi: " + chatAIRequest.getQuestion() +
                "\nThông tin tham khảo để trả lời (nhớ biến link thành thẻ a): \n" + ragContext;
        String answer = callChatModel(getListMessages(thisConversationId, currentQuestionWithRag), true);
        if (answer == null || answer.isBlank()) {
            answer = "Yêu cầu của bạn đã được xử lý thành công!";
        }
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

    private String callChatModel(List<Message> messages, boolean useTools) {
        try {
            return retryTemplate.execute(context -> {
                var requestSpec = chatClient.prompt()
                        .messages(messages)
                        .options(OpenAiChatOptions.builder().model(primaryModel).build());
                if (useTools) {
                    requestSpec.toolNames("createRequestRentalForAi");
                }
                return requestSpec.call().content();
            });
        } catch (Exception e) {
            System.err.println("Primary model error (" + primaryModel + "): " + e.getMessage() + ". Switching to fallback model: " + fallbackModel);
            try {
                return retryTemplate.execute(context -> {
                    var requestSpec = chatClient.prompt()
                            .messages(messages)
                            .options(OpenAiChatOptions.builder().model(fallbackModel).build());
                    if (useTools) {
                        requestSpec.toolNames("createRequestRentalForAi");
                    }
                    return requestSpec.call().content();
                });
            } catch (Exception ex) {
                System.err.println("Fallback model error (" + fallbackModel + "): " + ex.getMessage());
                return "Hệ thống AI hiện đang quá tải hoặc gặp sự cố. Vui lòng thử lại sau vài phút.";
            }
        }
    }

    private List<Message> getListMessages(String conversationId, String currentQuestionWithRag) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage("""
        Bạn là trợ lý AI của trang web cho thuê phòng trọ. Trả lời ngắn gọn, chính xác, KHÔNG dùng markdown.
        THỜI GIAN HIỆN TẠI HỆ THỐNG: """ + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME) + """
        . Hãy lấy thời gian này làm mốc để xác định năm, tháng, ngày hiện tại.

        Khi thông tin tham khảo có chứa link (bắt đầu bằng http://), hãy biến link đó thành thẻ HTML <a> theo đúng định dạng sau:
        <a href="LINK_URL">Xem chi tiết tại đây</a>
        Chỉ xuất ra thẻ <a> hoàn chỉnh, KHÔNG viết thêm bất kỳ text nào xung quanh thẻ đó như "chi tiết", "xem", v.v.

        === QUY TẮC SỬ DỤNG TOOL ===

        [ĐẶT LỊCH HẸN]
        Khi người dùng muốn đặt lịch hẹn xem phòng, thực hiện đúng thứ tự:
        1. LUÔN LUÔN gọi getRentalRequestsForAi() để lấy TRẠNG THÁI MỚI NHẤT của các yêu cầu thuê. TUYỆT ĐỐI KHÔNG dùng trạng thái PENDING từ trong lịch sử chat vì chủ nhà có thể vừa mới duyệt xong tức thì.
        2. Hiển thị danh sách, hỏi user chọn yêu cầu nào.
        3. Hỏi thời gian hẹn (nếu chưa có). ĐẶC BIỆT LƯU Ý: Khi gọi hàm scheduleAppointmentForAi, tham số appointmentTime BẮT BUỘC phải truyền đúng định dạng yyyy-MM-ddTHH:mm:ss (Ví dụ: 2026-06-13T15:00:00). Bạn hãy tự động parse câu nói của người dùng (VD: "15h00 ngày 13/6") ra định dạng này.
        4. Hỏi địa điểm (nếu chưa có).
        5. Gọi scheduleAppointmentForAi() với đầy đủ thông tin.
        KHÔNG yêu cầu user tự nhập rentalRequestId. KHÔNG gọi scheduleAppointmentForAi() trước bước 2.

        [THUÊ PHÒNG]
        Khi người dùng muốn thuê phòng:
        - Lập tức gọi createRequestRentalForAi() ngay, truyền null cho address và idCardNumber (backend sẽ tự động tìm trong Database).
        - CHỈ KHI NÀO tool trả về thông báo yêu cầu cung cấp thông tin, lúc đó mới hỏi người dùng Địa chỉ và CCCD.

        [DUYỆT / TỪ CHỐI YÊU CẦU]
        Khi người dùng muốn duyệt hoặc từ chối:
        - Nếu chưa có rentalRequestId → gọi getTodayRentalRequestsForAi() để lấy danh sách, hỏi user chọn.
        - Sau khi có ID → gọi approveRentalRequestForAi().
        """));
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
        try {
            String rewrittenQuery = callChatModel(rewriteMessages, false);

            // ✅ Guard null — nếu AI trả null thì dùng câu gốc
            if (rewrittenQuery == null || rewrittenQuery.isBlank()) {
                System.out.println("=== rewriteQuestion trả null, dùng câu gốc");
                return question;
            }

            System.out.println("=== CÂU HỎI GỐC: " + question);
            System.out.println("=== CÂU ĐÃ ĐƯỢC AI DỊCH: " + rewrittenQuery);
            return rewrittenQuery;

        } catch (Exception e) {
            System.out.println("=== rewriteQuestion lỗi, dùng câu gốc: " + e.getMessage());
            return question; // ✅ fallback về câu gốc
        }
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
            String url = "\nLink: http://localhost:8081/rooms/" + roomId;
            return content + url;
        }).collect(Collectors.joining("\n"));
        return answer;
    }
}
