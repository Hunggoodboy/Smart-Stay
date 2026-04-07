package vn.edu.ptit.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.ptit.dto.Request.RentalRequestDTO;
import vn.edu.ptit.service.RentalService;

@RestController
@AllArgsConstructor
public class RentalController {
    private final RentalService rentalService;

    @PostMapping("/api/rental-requests")
    public ResponseEntity<?> createRentalRequest(@RequestBody RentalRequestDTO rentalRequestDTO){
        return ResponseEntity.ok(rentalService.createNewRentalRequest(rentalRequestDTO));
    }
}
