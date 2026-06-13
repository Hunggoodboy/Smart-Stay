package vn.edu.ptit.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.ptit.dto.Request.ChangePasswordRequest;
import vn.edu.ptit.dto.Request.UpgradeCustomerRequest;
import vn.edu.ptit.dto.Request.UpdateProfileRequest;
import vn.edu.ptit.dto.Response.ApiResponse;
import vn.edu.ptit.dto.Response.AuthResponse;
import vn.edu.ptit.dto.Request.LoginRequest;
import vn.edu.ptit.dto.Request.RegisterRequest;
import vn.edu.ptit.service.Authentication.AuthService;

import vn.edu.ptit.repository.RoomsRepository;
import vn.edu.ptit.entity.User;

@RestController
@AllArgsConstructor
@RequestMapping("/api/user")
public class AuthController {
    private final AuthService authService;
    private final RoomsRepository roomsRepository;
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest, HttpServletResponse httpServletResponse, HttpServletRequest httpServletRequest) {
        AuthResponse authResponse = authService.register(registerRequest);
        if(!authResponse.isSuccess()){
            return ResponseEntity.badRequest().body(authResponse);
        }
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse httpServletResponse, HttpServletRequest httpServletRequest) {
        AuthResponse authResponse = authService.login(loginRequest, httpServletRequest, httpServletResponse);
        if(!authResponse.isSuccess()){
            return ResponseEntity.badRequest().body(authResponse);
        }
        return ResponseEntity.ok(authResponse);
    }


    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpServletRequest request, HttpServletResponse response) {
        ApiResponse result = authService.logout(request, response);
        return ResponseEntity.ok(result);

    }
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        AuthResponse authResponse = authService.generateTokenByRefreshToken(request, response);
        if (!authResponse.isSuccess()) {
            return ResponseEntity.status(401).body(authResponse);
        }
        return ResponseEntity.ok(authResponse);
    }
    @PostMapping("upgrade/customer")
    public ResponseEntity<?> upgradeToCustomer(@RequestBody UpgradeCustomerRequest upgradeCustomerRequest) {
        ApiResponse authResponse = authService.upgradeCustomer(upgradeCustomerRequest);
        if(!authResponse.isSuccess()){
            return ResponseEntity.badRequest().body(authResponse);
        }
        return ResponseEntity.ok(authResponse);
    }
    @GetMapping("/tenant")
    public ResponseEntity<?> getTenant() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }

    @PutMapping(value = "/profile", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest request) {
        try {
            return ResponseEntity.ok(authService.updateCurrentUser(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());
        }
    }

    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfileForm(@ModelAttribute UpdateProfileRequest request,
                                               @RequestPart(value = "avatarFile", required = false) MultipartFile avatarFile) {
        try {
            return ResponseEntity.ok(authService.updateCurrentUser(request, avatarFile));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());
        }
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        ApiResponse response = authService.changePassword(request);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }


    @GetMapping("/myid")
    public ResponseEntity<?> getCurrentUserId() {
        return ResponseEntity.ok(authService.getCurrentUser().getId());
    }

    @GetMapping("/landlord-id")
    public ResponseEntity<?> getLandLordId() {
        Long tenantId = authService.getCurrentUser().getId();
        User landlord = roomsRepository.findLandLordByCustomerId(tenantId).orElse(null);
        if (landlord != null) {
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("id", landlord.getId());
            data.put("fullName", landlord.getFullName());
            data.put("avatarUrl", landlord.getAvatarUrl());
            return ResponseEntity.ok(data);
        }
        return ResponseEntity.badRequest().body("Landlord not found");
    }

    @GetMapping("/has-room")
    public ResponseEntity<Boolean> hasRoom() {
        try {
            Long userId = authService.getCurrentUserId();
            return ResponseEntity.ok(roomsRepository.existsActiveRentalByCustomerId(userId));
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(authService.getUserById(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
