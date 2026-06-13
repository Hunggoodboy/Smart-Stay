package vn.edu.ptit.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.ptit.dto.Request.AppointmentRequest;
import vn.edu.ptit.dto.Response.ApiResponse;
import vn.edu.ptit.dto.Response.AppointmentResponse;
import vn.edu.ptit.dto.Response.AppointmentSummaryResponse;
import vn.edu.ptit.service.room.AppointmentService;
import vn.edu.ptit.service.Authentication.AuthService;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AuthService authService;

    /**
     * Tạo lịch hẹn mới (Chủ nhà hoặc người thuê)
     */
    @PostMapping
    public ResponseEntity<?> createAppointment(@Valid @RequestBody AppointmentRequest request) throws BadRequestException {
        try {
            Long userId = authService.getCurrentUserId();
            AppointmentResponse response = appointmentService.createAppointment(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }

    /**
     * Lấy danh sách lịch hẹn của tôi (Summary)
     */
    @GetMapping
    public ResponseEntity<?> getMyAppointments() {
        try {
            Long userId = authService.getCurrentUserId();
            List<AppointmentSummaryResponse> responses = appointmentService.getMyAppointments(userId);
            return ResponseEntity.ok(responses);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }

    /**
     * Lấy chi tiết một lịch hẹn
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getAppointmentById(@PathVariable Long id) {
        try {
            Long userId = authService.getCurrentUserId();
            AppointmentResponse response = appointmentService.getAppointmentById(id, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException | BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }

    /**
     * Cập nhật lịch hẹn
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAppointment(@PathVariable Long id, @Valid @RequestBody AppointmentRequest request) throws BadRequestException {
        try {
            Long userId = authService.getCurrentUserId();
            AppointmentResponse response = appointmentService.updateAppointment(id, request, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }

    /**
     * Xác nhận/Đồng ý lịch hẹn
     */
    @PostMapping("/{id}/confirm")
    public ResponseEntity<?> confirmAppointment(@PathVariable Long id) throws BadRequestException {
        try {
            Long userId = authService.getCurrentUserId();
            AppointmentResponse response = appointmentService.confirmAppointment(id, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }

    /**
     * Hủy lịch hẹn
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelAppointment(@PathVariable Long id) throws BadRequestException {
        try {
            Long userId = authService.getCurrentUserId();
            AppointmentResponse response = appointmentService.cancelAppointment(id, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }

    /**
     * Hoàn tất lịch hẹn (Sau khi gặp mặt thành công)
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeAppointment(@PathVariable Long id) throws BadRequestException {
        try {
            Long userId = authService.getCurrentUserId();
            AppointmentResponse response = appointmentService.completeAppointment(id, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }

    /**
     * Xoá lịch hẹn (chỉ được xoá khi đã CANCELLED hoặc COMPLETED)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAppointment(@PathVariable Long id) {
        try {
            Long userId = authService.getCurrentUserId();
            appointmentService.deleteAppointment(id, userId);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("Đã xoá lịch hẹn thành công")
                    .build());
        } catch (RuntimeException | BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        }
    }
}
