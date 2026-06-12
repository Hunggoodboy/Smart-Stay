package vn.edu.ptit.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.ptit.service.admin.AdminSystemStatsService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin")
@AllArgsConstructor
public class AdminSystemStatsController {

    private final AdminSystemStatsService adminSystemStatsService;

    @GetMapping("/system-stats")
    public ResponseEntity<?> getSystemStats(
            @RequestParam(required = false) Integer year
    ) {
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        return ResponseEntity.ok(adminSystemStatsService.getSystemStats(year));
    }
}
