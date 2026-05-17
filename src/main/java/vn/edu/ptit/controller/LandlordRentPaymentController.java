package vn.edu.ptit.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.ptit.dto.Request.ConfirmRentPaymentRequest;
import vn.edu.ptit.dto.Response.ApiResponse;
import vn.edu.ptit.dto.Response.RentPaymentResponse;
import vn.edu.ptit.entity.RentPayments;
import vn.edu.ptit.service.LandlordRentPaymentService;

import java.util.List;

@RestController
@RequestMapping("/api/landlord/rent-payments")
@AllArgsConstructor
public class LandlordRentPaymentController {
    private final LandlordRentPaymentService landlordRentPaymentService;

    @GetMapping
    public ResponseEntity<List<RentPaymentResponse>> getPayments(@RequestParam(required = false) RentPayments.Status status) {
        return ResponseEntity.ok(landlordRentPaymentService.getPaymentsForCurrentLandlord(status));
    }

    @GetMapping("/unpaid")
    public ResponseEntity<List<RentPaymentResponse>> getUnpaidPayments() {
        return ResponseEntity.ok(landlordRentPaymentService.getUnpaidPaymentsForCurrentLandlord());
    }

    @GetMapping("/paid")
    public ResponseEntity<List<RentPaymentResponse>> getPaidPayments() {
        return ResponseEntity.ok(landlordRentPaymentService.getPaymentsForCurrentLandlord(RentPayments.Status.PAID));
    }

    @PatchMapping("/{paymentId}/paid")
    public ResponseEntity<?> markAsPaid(
            @PathVariable Long paymentId,
            @RequestBody(required = false) ConfirmRentPaymentRequest request
    ) {
        try {
            ApiResponse response = landlordRentPaymentService.markAsPaid(paymentId, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.builder().success(false).message(ex.getMessage()).build());
        }
    }
}
