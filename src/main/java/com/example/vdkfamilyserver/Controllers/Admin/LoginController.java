package com.example.vdkfamilyserver.Controllers.Admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login"; // вернёт templates/login.html
    }

    @GetMapping("/logout")
    public String logout() {
        return "login";
    }
}