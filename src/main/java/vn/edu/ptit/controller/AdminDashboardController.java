package vn.edu.ptit.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.ptit.service.admin.AdminDashboardService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/users/total")
    public ResponseEntity<?> getTotalUsers() {
        return ResponseEntity.ok(adminDashboardService.getTotalUsers());
    }

    @GetMapping("/users/customers/total")
    public ResponseEntity<?> getTotalCustomers() {
        return ResponseEntity.ok(adminDashboardService.getTotalCustomers());
    }

    @GetMapping("/users/landlords/total")
    public ResponseEntity<?> getTotalLandlords() {
        return ResponseEntity.ok(adminDashboardService.getTotalLandlords());
    }

    @GetMapping("/landlords/pending-verifications/total")
    public ResponseEntity<?> getPendingLandlordVerifications() {
        return ResponseEntity.ok(adminDashboardService.getPendingLandlordVerifications());
    }

    @GetMapping("/landlords/verified/total")
    public ResponseEntity<?> getVerifiedLandlords() {
        return ResponseEntity.ok(adminDashboardService.getVerifiedLandlords());
    }

    @GetMapping("/room-posts/need-review/total")
    public ResponseEntity<?> getPostsNeedReview() {
        return ResponseEntity.ok(adminDashboardService.getPostsNeedReview());
    }

    @GetMapping("/room-posts/total")
    public ResponseEntity<?> getTotalRoomPosts() {
        return ResponseEntity.ok(adminDashboardService.getTotalRoomPosts());
    }

    @GetMapping("/rooms/total")
    public ResponseEntity<?> getTotalRooms() {
        return ResponseEntity.ok(adminDashboardService.getTotalRooms());
    }
}
