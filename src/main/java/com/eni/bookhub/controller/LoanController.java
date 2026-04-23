package com.eni.bookhub.controller;

import com.eni.bookhub.dto.request.LoanRequest;
import com.eni.bookhub.dto.response.LoanResponse;
import com.eni.bookhub.service.LoanService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/loans")
public class LoanController {

    private final LoanService service;

    public LoanController(LoanService service) {
        this.service = service;
    }

    // Emprunter un livre
    @PostMapping
    public LoanResponse borrowBook(@RequestBody LoanRequest request) {
        return service.borrowBook(request.getUserId(), request.getBookId());
    }

    @GetMapping("/me")
    public ResponseEntity<List<LoanResponse>> getMyActiveLoans(Authentication auth) {
        return ResponseEntity.ok(service.getActiveLoans(auth.getName()));
    }
}