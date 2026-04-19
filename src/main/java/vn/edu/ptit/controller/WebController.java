package vn.edu.ptit.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class WebController {
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

    @GetMapping("/incident-report")
    public String incidentReport() {
        return "incidentReport";
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
}
