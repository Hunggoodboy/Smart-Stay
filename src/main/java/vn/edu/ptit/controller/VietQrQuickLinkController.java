package vn.edu.ptit.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.ptit.dto.Response.ApiResponse;
import vn.edu.ptit.dto.Response.VietQrQuickLinkResponse;
import vn.edu.ptit.service.VietQrQuickLinkService;

@RestController
@RequestMapping("/api/payments/vietqr")
@RequiredArgsConstructor
public class VietQrQuickLinkController {

    private final VietQrQuickLinkService vietQrQuickLinkService;

    @GetMapping("/quicklink")
    public ResponseEntity<?> getQuickLink() {
        try {
            VietQrQuickLinkResponse response = vietQrQuickLinkService.buildQuickLinkForCurrentUser();
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .success(false)
                    .message(ex.getMessage())
                    .build());
        }
    }
}
