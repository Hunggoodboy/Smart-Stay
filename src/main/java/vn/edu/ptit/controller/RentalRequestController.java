package vn.edu.ptit.controller;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.ptit.dto.Request.RentalRequestDTO;
import vn.edu.ptit.dto.Response.RentalRequestResponse;
import vn.edu.ptit.entity.RentalRequests;
import vn.edu.ptit.service.RentalRequestService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class RentalRequestController {
    private final RentalRequestService   rentalRequestsService;

    @GetMapping("/landlord/requests")
    public ResponseEntity<List<RentalRequestResponse> > getRentalRequests() {
        return ResponseEntity.ok(rentalRequestsService.findRentalRequestByLandLordId());
    }

    @PostMapping("/customer/requests")
    public ResponseEntity<?> createRentalRequest(@RequestBody RentalRequestDTO rentalRequestDTO){
        return ResponseEntity.ok(rentalRequestsService.createNewRentalRequest(rentalRequestDTO));
    }

    @PostMapping("/landlord/changeRequestStatus/{requestId}")
    public ResponseEntity<?> changeRequestStatus(@PathVariable Long requestId, @RequestParam String status) {
        return ResponseEntity.ok(rentalRequestsService.changeRequestStatus(requestId, status));
    }
}
