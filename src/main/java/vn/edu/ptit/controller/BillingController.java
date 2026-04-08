package vn.edu.ptit.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.edu.ptit.dto.MonthlyBillRequest;
import vn.edu.ptit.dto.MonthlyBillResponse;
import vn.edu.ptit.service.BillingService;

@RestController
@RequestMapping("/api/billing")
@CrossOrigin
public class BillingController {

    @Autowired
    private BillingService billingService;

    @PostMapping("/preview")
    public ResponseEntity<?> previewMonthlyBill(@RequestBody MonthlyBillRequest request) {
        try {
            MonthlyBillResponse response = billingService.previewBill(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/monthly")
    public ResponseEntity<?> createMonthlyBill(@RequestBody MonthlyBillRequest request) {
        try {
            MonthlyBillResponse response = billingService.createBill(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }
}