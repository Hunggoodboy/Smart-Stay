package vn.edu.ptit.controller;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.ptit.dto.Request.CreateRoomManageRequest;
import vn.edu.ptit.dto.Request.RentalRequestDTO;
import vn.edu.ptit.dto.Response.ApiResponse;
import vn.edu.ptit.dto.Response.RentalRequestResponse;
import vn.edu.ptit.entity.RentalRequests;
import vn.edu.ptit.service.RentalRequestService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class RentalRequestController {
    private final RentalRequestService  rentalRequestsService;

    @GetMapping("/my/requests")
    public ResponseEntity<List<RentalRequestResponse>> getRentalRequests() {
        return ResponseEntity.ok(rentalRequestsService.findMyRequests());
    }


    @PostMapping("/customer/requests")
    public ResponseEntity<?> createRentalRequest(@RequestBody RentalRequestDTO rentalRequestDTO){
        try {
            return ResponseEntity.ok(rentalRequestsService.createNewRentalRequest(rentalRequestDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(vn.edu.ptit.dto.Response.ApiResponse.builder()
                            .message(e.getMessage())
                            .success(false)
                            .build());
        }
    }

    @PostMapping("/landlord/changeRequestStatus/{requestId}")
    public ResponseEntity<?> changeRequestStatus(@PathVariable Long requestId, @RequestParam String status) {
        return ResponseEntity.ok(rentalRequestsService.changeRequestStatus(requestId, status));
    }

    @DeleteMapping("/delete/request/{requestId}")
    public ResponseEntity<?> deleteRequest(@PathVariable Long requestId) {
        try {
            return ResponseEntity.ok(rentalRequestsService.deleteRequest(requestId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(vn.edu.ptit.dto.Response.ApiResponse.builder()
                            .message(e.getMessage())
                            .success(false)
                            .build());
        }
    }

    @GetMapping("/room-management/recomendation/{requestId}")
    public ResponseEntity<?> getRoomManagementRecomendation(@PathVariable Long requestId) {
        try{
            return ResponseEntity.ok(rentalRequestsService.getRecommendedRentalRequest(requestId));
        }
        catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message("Bạn không được gửi gợi ý để tạo phòng")
                            .build());
        }
    }

}
