package vn.edu.ptit.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.ptit.dto.Response.AuthResponse;
import vn.edu.ptit.dto.Request.LoginRequest;
import vn.edu.ptit.dto.Request.RegisterRequest;
import vn.edu.ptit.service.Authentication.AuthService;

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
    public ResponseEntity<?> getTenant() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }


    @GetMapping("/myid")
    public ResponseEntity<?> getCurrentUserId() {
        return ResponseEntity.ok(authService.getCurrentUser().getId());
    }
}
