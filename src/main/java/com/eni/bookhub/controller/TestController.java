package com.eni.bookhub.controller;

import com.eni.bookhub.service.JwtService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    private final JwtService jwtService;

    public TestController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @GetMapping("/api/test-token")
    public String testToken() {
        return jwtService.generateToken("test@mail.com", "UTILISATEUR");
    }

    @GetMapping("/api/secure")
    public String secure() {
        return "OK sécurisé";
    }
}