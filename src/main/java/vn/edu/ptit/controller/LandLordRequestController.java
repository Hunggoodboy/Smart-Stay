package vn.edu.ptit.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.ptit.dto.Response.ApiResponse;
import vn.edu.ptit.service.Authentication.LandLordService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/admin/landlord")
public class LandLordRequestController {
    private LandLordService landLordService;

    @GetMapping("")
    public ResponseEntity<?> getAllLandLordRequest() {
        return ResponseEntity.ok(landLordService.getAllLandLordsRequest());
    }

    @GetMapping("/verifyTrue")
    public ResponseEntity<?> getLandLordVerifyTrue() {
        return ResponseEntity.ok(landLordService.getLandLordsByVerified(true));
    }
    @GetMapping("/verifyFalse")
    public ResponseEntity<?> getLandLordVerifyFalse() {
        return ResponseEntity.ok(landLordService.getLandLordsByVerified(false));
    }
    @PostMapping("/verify/{id}")
    public ResponseEntity<ApiResponse> verifyLandLord(@PathVariable Long id) {
        return ResponseEntity.ok(landLordService.verifyLandLord(id));
    }
}
