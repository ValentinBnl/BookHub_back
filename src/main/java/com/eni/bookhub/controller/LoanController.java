package com.eni.bookhub.controller;

import com.eni.bookhub.dto.request.LoanRequest;
import com.eni.bookhub.dto.response.LoanResponse;
import com.eni.bookhub.service.LoanService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/loans")
public class LoanController {

    private final LoanService service;

    public LoanController(LoanService service) {
        this.service = service;
    }

    // Emprunter un livre
    @PostMapping
    @PreAuthorize("hasAnyRole('LIBRAIRE', 'ADMIN')")
    public LoanResponse borrowBook(@RequestBody LoanRequest request) {
        return service.borrowBook(request.getUserId(), request.getBookId());
    }

    @GetMapping("/me")
    public ResponseEntity<List<LoanResponse>> getMyLoans(Authentication auth) {
        return ResponseEntity.ok(service.getUserLoans(auth.getName()));
    }

    // Retourner un livre
    @PutMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('LIBRAIRE', 'ADMIN')")
    public LoanResponse returnBook(@PathVariable("id") Integer id) {
        return service.returnBook(id);
    }
}