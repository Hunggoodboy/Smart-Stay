package vn.edu.ptit.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

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

    @GetMapping("/")
    public String home() {
        return "index";
    }
}
