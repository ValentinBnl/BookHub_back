package com.eni.bookhub.controller;

import com.eni.bookhub.dto.request.ReservationRequest;
import com.eni.bookhub.dto.response.ReservationResponse;
import com.eni.bookhub.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            Authentication auth,
            @Valid @RequestBody ReservationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.createReservation(auth.getName(), request));
    }

    @GetMapping("/me")
    public ResponseEntity<List<ReservationResponse>> getMyReservations(Authentication auth) {
        return ResponseEntity.ok(reservationService.getMyReservations(auth.getName()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('LIBRAIRE', 'ADMIN')")
    public ResponseEntity<List<ReservationResponse>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelReservation(Authentication auth, @PathVariable Integer id) {
        boolean isStaff = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_LIBRAIRE") || a.equals("ROLE_ADMIN"));
        reservationService.cancelReservation(auth.getName(), id, isStaff);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/validate")
    @PreAuthorize("hasAnyRole('LIBRAIRE', 'ADMIN')")
    public ResponseEntity<ReservationResponse> validateReservation(@PathVariable Integer id) {
        return ResponseEntity.ok(reservationService.validateReservation(id));
    }
}
