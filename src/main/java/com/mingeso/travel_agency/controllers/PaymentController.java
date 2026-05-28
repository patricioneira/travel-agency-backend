package com.mingeso.travel_agency.controllers;

import com.mingeso.travel_agency.entities.PaymentEntity;
import com.mingeso.travel_agency.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // [1]
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@CrossOrigin("*")
public class PaymentController {
    private final PaymentService paymentService;

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/")
    public ResponseEntity<PaymentEntity> processPayment(@RequestBody PaymentEntity payment) {
        PaymentEntity newPayment = paymentService.processPayment(payment);
        if (newPayment == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(newPayment);
    }
}