package com.mingeso.travel_agency.controllers;

import com.mingeso.travel_agency.entities.ReservationEntity;
import com.mingeso.travel_agency.services.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // [1]
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ReservationController {
    private final ReservationService reservationService;

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/")
    public ResponseEntity<ReservationEntity> createReservation(@RequestBody ReservationEntity reservation) {
        ReservationEntity newReservation = reservationService.createReservation(reservation);
        if (newReservation == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(newReservation);
    }
}