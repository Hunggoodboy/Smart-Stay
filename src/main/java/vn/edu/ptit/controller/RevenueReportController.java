package vn.edu.ptit.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.ptit.dto.Response.ApiResponse;
import vn.edu.ptit.service.RevenueReportService;

@RestController
@RequestMapping("/api/revenue-report")
@AllArgsConstructor
public class RevenueReportController {
    private final RevenueReportService revenueReportService;

    @GetMapping("/monthly")
    public ResponseEntity<?> getMonthlyReport(@RequestParam Integer year, @RequestParam Integer month) {
        try {
            return ResponseEntity.ok(revenueReportService.getMonthlyReport(year, month));
        } catch (RuntimeException e) {
            return badRequest(e.getMessage());
        }
    }

    @GetMapping("/year")
    public ResponseEntity<?> getYearReport(@RequestParam Integer year) {
        try {
            return ResponseEntity.ok(revenueReportService.getYearReport(year));
        } catch (RuntimeException e) {
            return badRequest(e.getMessage());
        }
    }

    private ResponseEntity<ApiResponse> badRequest(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.builder().success(false).message(message).build());
    }
}
