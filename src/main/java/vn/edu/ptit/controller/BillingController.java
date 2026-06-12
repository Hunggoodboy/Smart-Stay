package vn.edu.ptit.controller;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import vn.edu.ptit.dto.Request.MonthlyBillRequest;
import vn.edu.ptit.dto.Response.MonthlyBillResponse;
import vn.edu.ptit.service.room.BillingService;

@Slf4j
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
            String msg = ex.getMessage() != null ? ex.getMessage() : "Lỗi server khi tính toán hóa đơn";
            log.error("[Billing/preview] {}", msg, ex);
            return ResponseEntity.badRequest().body(Map.of("message", msg));
        } catch (Exception ex) {
            log.error("[Billing/preview] Unexpected error", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi hệ thống: " + ex.getClass().getSimpleName()));
        }
    }

    @PostMapping("/monthly")
    public ResponseEntity<?> createMonthlyBill(@RequestBody MonthlyBillRequest request) {
        try {
            MonthlyBillResponse response = billingService.createBill(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : "Lỗi server khi tạo hóa đơn";
            log.error("[Billing/monthly] {}", msg, ex);
            return ResponseEntity.badRequest().body(Map.of("message", msg));
        } catch (Exception ex) {
            log.error("[Billing/monthly] Unexpected error", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi hệ thống: " + ex.getClass().getSimpleName()));
        }
    }

    @GetMapping("/suggestion")
    public ResponseEntity<?> getSuggestion(@RequestParam Long roomId) {
        return ResponseEntity.ok(billingService.createSuggestionBillingResponse(roomId));
    }

    @GetMapping("/suggestion/before-month")
    public ResponseEntity<?> getSuggestionBeforeMonth(@RequestParam Long roomId, @RequestParam String month) {
        return ResponseEntity.ok(billingService.getBillingLastMonthResponse(roomId, month));
    }
}