package vn.edu.ptit.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.ptit.dto.Request.CreateNotificationRequest;
import vn.edu.ptit.dto.Response.NotificationsResponse;
import vn.edu.ptit.service.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@AllArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping("/create")
    public ResponseEntity<?> createNewNotification(@RequestBody CreateNotificationRequest request){
        return ResponseEntity.ok(notificationService.createNotification(request));
    }

    @GetMapping
    public ResponseEntity<List<NotificationsResponse> > getAllNotifications() {
        List<NotificationsResponse> notifications = notificationService.getNotificationResponseForUsers();
        return ResponseEntity.ok(notifications);
    }
}
