package vn.edu.ptit.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.ptit.dto.Request.RentalRequestDTO;
import vn.edu.ptit.dto.Request.UpdateRentalRequestStatusRequest;
import vn.edu.ptit.dto.Response.RentalRequestResponse;
import vn.edu.ptit.service.RentalRequestService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class RentalRequestController {
    private final RentalRequestService rentalRequestsService;

    @GetMapping("/landlord/requests")
    public ResponseEntity<List<RentalRequestResponse>> getRentalRequests() {
        return ResponseEntity.ok(rentalRequestsService.findRentalRequestByLandLordId());
    }

    @PatchMapping("/landlord/requests/{id}")
    public ResponseEntity<?> updateRentalRequestStatus(@PathVariable Long id,
            @RequestBody UpdateRentalRequestStatusRequest request) {
        return ResponseEntity.ok(rentalRequestsService.updateRequestStatus(id, request.getStatus()));
    }

    @PostMapping("/api/customer/requests")
    public ResponseEntity<?> createRentalRequest(@RequestBody RentalRequestDTO rentalRequestDTO) {
        return ResponseEntity.ok(rentalRequestsService.createNewRentalRequest(rentalRequestDTO));
    }
}
