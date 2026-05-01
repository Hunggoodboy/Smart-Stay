package vn.edu.ptit.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import vn.edu.ptit.service.Authentication.AuthService;

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
    public String roomDetail(@PathVariable Long id) {
        return "roomDetail";
    }

    @GetMapping("/MyRentalRequest")
    public String rentalRequest() {
        return "rentalRequests";
    }

    @GetMapping("/adminVerify")
    public String adminVerify() {
        return "adminVerify";
    }

    @GetMapping("/registerLandLord")
    public String registerLandLord() {
        return "registerLandlord";
    }

    @GetMapping("/landlord-view")
    public String landlordView() {
        return "landlord_index";
    }
}
