package com.mingeso.travel_agency.controllers;

import com.mingeso.travel_agency.entities.ReservationEntity;
import com.mingeso.travel_agency.services.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ReservationController {
    private final ReservationService reservationService;

    // Crear reserva (usuario autenticado)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/")
    public ResponseEntity<ReservationEntity> createReservation(@RequestBody ReservationEntity reservation) {
        try {
            ReservationEntity newReservation = reservationService.createReservation(reservation);
            return ResponseEntity.ok(newReservation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Ver detalle de una reserva
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ReservationEntity> getReservationById(@PathVariable Long id) {
        ReservationEntity reservation = reservationService.getReservationById(id);
        if (reservation == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(reservation);
    }

    // Cliente ve sus propias reservas
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReservationEntity>> getReservationsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(reservationService.getReservationsByUser(userId));
    }

    // Admin ve todas las reservas
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/")
    public ResponseEntity<List<ReservationEntity>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    // Cancelar reserva
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ReservationEntity> cancelReservation(@PathVariable Long id) {
        ReservationEntity cancelled = reservationService.cancelReservation(id);
        if (cancelled == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(cancelled);
    }
}
