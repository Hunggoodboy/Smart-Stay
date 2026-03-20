package vn.edu.ptit.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.ptit.dto.Response.NotificationsResponse;
import vn.edu.ptit.service.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@AllArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationsResponse> > getAllNotifications() {
        List<NotificationsResponse> notifications = notificationService.getNotificationResponseForUsers();
        return ResponseEntity.ok(notifications);
    }
}
