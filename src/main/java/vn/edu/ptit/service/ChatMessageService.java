package vn.edu.ptit.service;


import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import vn.edu.ptit.dto.Request.ChatMessageRequest;
import vn.edu.ptit.dto.Response.ChatMessagesResponse;
import vn.edu.ptit.dto.Response.ConversationResponse;
import vn.edu.ptit.entity.ChatMessages;
import vn.edu.ptit.entity.User;
import vn.edu.ptit.repository.*;
import vn.edu.ptit.service.Authentication.AuthService;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ChatMessageService {
    private final ChatMessagesRepository chatMessagesRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final AuthService authService;
    private final RoomsRepository roomsRepository;
    public void saveMessage(ChatMessageRequest request) {
        System.out.println(">>> Request: " + request);
        System.out.println(">>> senderId: " + request.getSenderId());
        System.out.println(">>> receiverId: " + request.getReceiverId());
        ChatMessages chatMessage = new ChatMessages();
        if(request.getChatRoomId() != null) {
            chatMessage.setChatRoom(chatRoomRepository.findById(request.getChatRoomId()).orElseThrow(() -> new RuntimeException("Chat room not found")));
        }

        chatMessage.setContent(request.getContent());
        chatMessage.setMessageType(request.getMessageType());
        chatMessage.setSender(userRepository.findById(request.getSenderId()).orElseThrow( () -> new RuntimeException("Sender not found")));
        chatMessage.setReceiver(userRepository.findById(request.getReceiverId()).orElseThrow( () -> new RuntimeException("Receiver not found")));
        chatMessage.setCreatedAt(LocalDateTime.now());
        chatMessage.setSentAt(LocalDateTime.now());
        chatMessagesRepository.save(chatMessage);
    }
    public void sendPrivateMessage(ChatMessageRequest request, Principal principal) {
        User sender = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        // Nếu frontend không gửi receiverId → tự tìm chủ nhà
        User receiver;
        if (request.getReceiverId() == null || request.getReceiverId() == 0) {
            receiver = roomsRepository.findLandLordByCustomerId(sender.getId())
                    .orElseThrow(() -> new RuntimeException("Landlord not found"));
        } else {
            receiver = userRepository.findById(request.getReceiverId())
                    .orElseThrow(() -> new RuntimeException("Receiver not found"));
        }

        // Gán lại để saveMessage có đủ thông tin
        request.setSenderId(sender.getId());
        request.setReceiverId(receiver.getId());
        request.setSentAt(LocalDateTime.now());

        saveMessage(request);

        ChatMessagesResponse response = ChatMessagesResponse.builder()
                .id(request.getId())
                .chatRoomId(request.getChatRoomId())
                .content(request.getContent())
                .messageType(request.getMessageType())
                .senderType(request.getSenderType())
                .senderId(sender.getId())
                .senderName(sender.getFullName())
                .receiverId(receiver.getId())
                .sentAt(request.getSentAt())
                .build();

        messagingTemplate.convertAndSendToUser(receiver.getUsername(), "/queue/private", response);
        messagingTemplate.convertAndSendToUser(sender.getUsername(),   "/queue/private", response);
    }
    public List<ConversationResponse> getConversations() {
        Long userId = authService.getCurrentUserId();
        List<ChatMessages> chatMessages = chatMessagesRepository.findLatestMessagePerConversation(userId);
        List<ConversationResponse> conversationResponses = new ArrayList<>();
        for (ChatMessages msg : chatMessages) {
            Long senderId = msg.getSender().getId() != null ? msg.getSender().getId() : null;
            Long receiverId = msg.getReceiver().getId() != null ? msg.getReceiver().getId() : null;
            Long partnerId = senderId.equals(userId) ? receiverId : senderId;
            String partnerName = userRepository.findFullNameById(partnerId);
            ConversationResponse conversationResponse = ConversationResponse.builder()
                    .partnerId(partnerId)
                    .partnerName(partnerName)
                    .lastMessage(msg.getContent())
                    .lastMessageAt(msg.getSentAt())
                    .unreadCount(chatMessagesRepository.countUnread(userId, partnerId))
                    .build();
            conversationResponses.add(conversationResponse);
        }
        return conversationResponses;
    }
    public void loadMessageHistory(Long senderId, Long receiverId) {
        // Lấy lịch sử tin nhắn từ database
        List<ChatMessages> chatMessages = chatMessagesRepository.findAllBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderBySentAtAsc(senderId, receiverId, receiverId, senderId);
        List<ChatMessagesResponse> response = chatMessages.stream().map(msg -> ChatMessagesResponse.builder()
                .id(msg.getId())
                .chatRoomId(msg.getChatRoom() != null ? msg.getChatRoom().getId() : null)
                .content(msg.getContent())
                .messageType(msg.getMessageType())
                .senderId(msg.getSender() != null ? msg.getSender().getId() : (msg.getLandLord() != null ? msg.getLandLord().getId() : null))
                .senderName(msg.getSender() != null ? userRepository.findFullNameById(msg.getSender().getId()) : (msg.getLandLord() != null ? userRepository.findFullNameById(msg.getLandLord().getId()) : ""))
                .receiverId(msg.getReceiver() != null ? msg.getReceiver().getId() : null)
                .sentAt(msg.getSentAt())
                .build()).toList();

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));
        // Lấy tin nhắn cũ đến người nhận qua WebSocket
        messagingTemplate.convertAndSendToUser(
                receiver.getUsername(),
                "/queue/history",
                response
        );
        // Gửi tin nhắn đến người gửi qua WebSocket
        messagingTemplate.convertAndSendToUser(
                sender.getUsername(),
                "/queue/history",
                response
        );
    }
}
