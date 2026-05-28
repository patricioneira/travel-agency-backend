package com.mingeso.travel_agency.services;

import com.mingeso.travel_agency.entities.PaymentEntity;
import com.mingeso.travel_agency.entities.ReservationEntity;
import com.mingeso.travel_agency.repositories.PaymentRepository;
import com.mingeso.travel_agency.repositories.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public PaymentEntity processPayment(PaymentEntity payment) {
        ReservationEntity reservation = reservationRepository
                .findById(payment.getReservationId()).orElse(null);

        if (reservation == null || !"PENDING_PAYMENT".equals(reservation.getStatus())) {
            return null;
        }

        // Solo un pago por reserva
        if (paymentRepository.findByReservationId(payment.getReservationId()).isPresent()) {
            return null;
        }

        // Monto debe ser mayor que cero
        if (payment.getAmount() == null || payment.getAmount() <= 0) {
            return null;
        }

        // Monto debe cubrir el total de la reserva (no pagos parciales)
        if (payment.getAmount() < reservation.getFinalAmount()) {
            return null;
        }

        // Datos de tarjeta simulada obligatorios
        if (payment.getCardNumber() == null || payment.getExpirationDate() == null
                || payment.getCvv() == null) {
            return null;
        }

        payment.setStatus("APPROVED");
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentMethod("CREDIT_CARD");

        reservation.setStatus("CONFIRMED");
        reservationRepository.save(reservation);

        return paymentRepository.save(payment);
    }

    public PaymentEntity getPaymentByReservationId(Long reservationId) {
        return paymentRepository.findByReservationId(reservationId).orElse(null);
    }
}
