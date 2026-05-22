package vn.edu.ptit.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.ptit.dto.Request.AdminRoomPostFeaturedRequest;
import vn.edu.ptit.dto.Request.AdminRoomPostStatusRequest;
import vn.edu.ptit.dto.Request.AdminRoomStatusRequest;
import vn.edu.ptit.dto.Request.UpdateRoomPostRequest;
import vn.edu.ptit.entity.RoomPosts;
import vn.edu.ptit.entity.Rooms;
import vn.edu.ptit.service.AdminRoomPostManagementService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminRoomPostManagementController {
    private final AdminRoomPostManagementService managementService;

    @GetMapping("/room-posts")
    public ResponseEntity<?> getRoomPosts(
            @RequestParam(required = false) RoomPosts.Status status,
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(managementService.getRoomPosts(status, keyword));
    }

    @PatchMapping("/room-posts/{id}")
    public ResponseEntity<?> updateRoomPost(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoomPostRequest request
    ) {
        return ResponseEntity.ok(managementService.updateRoomPost(id, request));
    }

    @PatchMapping("/room-posts/{id}/status")
    public ResponseEntity<?> updateRoomPostStatus(
            @PathVariable Long id,
            @RequestBody AdminRoomPostStatusRequest request
    ) {
        return ResponseEntity.ok(managementService.updateRoomPostStatus(id, request));
    }

    @PatchMapping("/room-posts/{id}/featured")
    public ResponseEntity<?> updateRoomPostFeatured(
            @PathVariable Long id,
            @Valid @RequestBody AdminRoomPostFeaturedRequest request
    ) {
        return ResponseEntity.ok(managementService.updateRoomPostFeatured(id, request));
    }

    @GetMapping("/rooms")
    public ResponseEntity<?> getRooms(
            @RequestParam(required = false) Rooms.Status status,
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(managementService.getRooms(status, keyword));
    }

    @PatchMapping("/rooms/{id}/status")
    public ResponseEntity<?> updateRoomStatus(
            @PathVariable Long id,
            @RequestBody AdminRoomStatusRequest request
    ) {
        return ResponseEntity.ok(managementService.updateRoomStatus(id, request));
    }
}
