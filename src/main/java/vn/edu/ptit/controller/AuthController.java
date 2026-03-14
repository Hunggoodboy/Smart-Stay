package vn.edu.ptit.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.ptit.dto.AuthResponse;
import vn.edu.ptit.dto.LoginRequest;
import vn.edu.ptit.dto.RegisterRequest;
import vn.edu.ptit.repository.UserRepository;
import vn.edu.ptit.service.AuthService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/user")
public class AuthController {
    private final AuthService authService;
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

    @GetMapping("/tenant")
    public ResponseEntity<?> getTenant(Authentication authentication) {
        return ResponseEntity.ok(authService.getTenantDashboardData(authentication));
    }
}
