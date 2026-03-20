package vn.edu.ptit.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.ptit.dto.Response.NotificationsResponse;
import vn.edu.ptit.entity.Notifications;
import vn.edu.ptit.repository.NotificationsRepository;

import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor
public class NotificationService {
    private final NotificationsRepository notificationsRepository;
    private final AuthService authService;
    public List<NotificationsResponse> getNotificationResponseForUsers() {
        Long userId = authService.getCurrentUserId();
        List<Notifications> notificationsList = notificationsRepository.findAllByCustomerId(userId);
        return notificationsList.stream().map(
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
