package vn.edu.ptit.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.ptit.dto.AuthResponse;
import vn.edu.ptit.dto.LoginRequest;
import vn.edu.ptit.dto.RegisterRequest;
import vn.edu.ptit.repository.UserRepository;
import vn.edu.ptit.service.AuthService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/user")
public class AuthController {
    private final UserRepository userRepository;
    private final AuthService authService;
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        AuthResponse authResponse = authService.Register(registerRequest);
        if(!authResponse.isSuccess()){
            return ResponseEntity.badRequest().body(authResponse);
        }
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = authService.login(loginRequest);
        if(!authResponse.isSuccess()){
            return ResponseEntity.badRequest().body(authResponse);
        }
        return ResponseEntity.ok(authResponse);
    }
}
