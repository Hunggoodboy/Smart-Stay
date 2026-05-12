package vn.edu.ptit.controller;


import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.ptit.dto.Request.CreateRoomManageRequest;
import vn.edu.ptit.dto.Response.ApiResponse;
import vn.edu.ptit.service.RoomService;

@RestController
@RequestMapping("/api/room-management")
@AllArgsConstructor
public class RoomManagementController {
    private final RoomService roomService;

    @PostMapping("/create")
    public ApiResponse createNewRoom(@RequestBody CreateRoomManageRequest request) {
        return roomService.createRoomsManagement(request);
    }

    @GetMapping("/room-summary")
    public ResponseEntity<?>  getRoomsSummary() {
        return ResponseEntity.ok(roomService.getRoomsManagementSummary());
    }

    @GetMapping("/room-summary-deleted")
    public ResponseEntity<?>  getRoomsSummaryDeleted() {
        return ResponseEntity.ok(roomService.getRoomsManagementSummaryIsDeleted());
    }

    @GetMapping("/room-detail-management")
    public ResponseEntity<?>  getRoomsDetailManagement(@RequestParam Long roomId) {
        return ResponseEntity.ok(roomService.getRoomDetailManagement(roomId));
    }

}
