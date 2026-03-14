package vn.edu.ptit.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import vn.edu.ptit.service.AuthService;

@Controller
@AllArgsConstructor
public class WebController {
    private final AuthService authService;
    @GetMapping("login")
    public String login() {
        return "auth";
    }

    @GetMapping("register")
    public String register() {
        return "auth";
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }
}
