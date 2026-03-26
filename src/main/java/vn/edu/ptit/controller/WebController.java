package vn.edu.ptit.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/myHome")
    public String myHome() {
        return "index";
    }

    @GetMapping("/payment")
    public String payment() {
        return "payment";
    }
    @GetMapping("/chatMessage")
    public String chatMessage() {
        return "chatMessage";
    }

    @GetMapping("/postRooms")
    public String postNewRoom() {
        return "postNewRoom";
    }

    @GetMapping("/")
    public String roomsSummary() {
        return "roomsSummary";
    }

    @GetMapping("/rooms/{id}")
    public String roomDetais(@PathVariable Long id){
        return "roomDetail";
    }
}
