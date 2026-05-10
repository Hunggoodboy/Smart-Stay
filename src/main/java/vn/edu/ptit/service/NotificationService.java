package vn.edu.ptit.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.ptit.dto.Request.CreateNotificationRequest;
import vn.edu.ptit.dto.Response.ApiResponse;
import vn.edu.ptit.dto.Response.NotificationsResponse;
import vn.edu.ptit.entity.Notifications;
import vn.edu.ptit.repository.NotificationsRepository;
import vn.edu.ptit.repository.RoomsRepository;
import vn.edu.ptit.service.Authentication.AuthService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class NotificationService {
    private final NotificationsRepository notificationsRepository;
    private final AuthService authService;
    private final RoomsRepository roomsRepository;

    public ApiResponse createNotification(CreateNotificationRequest request) {
        Long userId = authService.getCurrentUserId();
        Notifications notifications = Notifications.builder()
                                                   .recipientType("CUSTOMER")
                                                   .title(request.getTitle())
                                                   .content(request.getContent())
                                                   .notificationType(request.getNotificationType())
                                                   .readAt(null)
                                                   .createdAt(LocalDateTime.now())
                                                   .updatedAt(LocalDateTime.now())
                                                   .landlord(authService.getCurrentLandLord())
                                                   .rooms(roomsRepository.findById(request.getRoomId()).orElseThrow(() -> new RuntimeException("Room not found")))
                                                   .build();
        notificationsRepository.save(notifications);
        return ApiResponse.builder()
                .message("Đã tạo thông báo thành công")
                .success(true)
                .build();
    }
        public List<NotificationsResponse> getNotificationResponseForUsers () {
            Long userId = authService.getCurrentUserId();
            List<Notifications> notificationsList = notificationsRepository.findAllByCustomerId(userId);
            return notificationsList.stream()
                                    .map(
                                            notification -> NotificationsResponse.builder()
                                                                                 .content(notification.getContent())
                                                                                 .notificationType(notification.getNotificationType())
                                                                                 .title(notification.getTitle())
                                                                                 .createdAt(notification.getCreatedAt())
                                                                                 .build()
                                    )
                                    .toList();
        }
    }
