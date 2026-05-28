package com.mingeso.travel_agency.services;

import com.mingeso.travel_agency.entities.PaymentEntity;
import com.mingeso.travel_agency.entities.ReservationEntity;
import com.mingeso.travel_agency.repositories.PaymentRepository;
import com.mingeso.travel_agency.repositories.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private ReservationRepository reservationRepository;

    @InjectMocks
    private PaymentService paymentService;

    private ReservationEntity reservation;
    private PaymentEntity payment;

    @BeforeEach
    void setUp() {
        reservation = new ReservationEntity();
        reservation.setId(1L);
        reservation.setFinalAmount(1500.0);
        reservation.setStatus("PENDING_PAYMENT");

        payment = new PaymentEntity();
        payment.setReservationId(1L);
        payment.setAmount(1500.0);
        payment.setCardNumber("4111111111111111");
        payment.setExpirationDate("12/27");
        payment.setCvv("123");
    }

    // --- Casos exitosos ---

    @Test
    void processPayment_withValidData_approvesPayment() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(paymentRepository.findByReservationId(1L)).thenReturn(Optional.empty());
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PaymentEntity result = paymentService.processPayment(payment);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("APPROVED");
    }

    @Test
    void processPayment_confirmsReservation() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(paymentRepository.findByReservationId(1L)).thenReturn(Optional.empty());
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        paymentService.processPayment(payment);

        assertThat(reservation.getStatus()).isEqualTo("CONFIRMED");
        verify(reservationRepository).save(reservation);
    }

    @Test
    void processPayment_setsPaymentDateAndMethod() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(paymentRepository.findByReservationId(1L)).thenReturn(Optional.empty());
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PaymentEntity result = paymentService.processPayment(payment);

        assertThat(result.getPaymentDate()).isNotNull();
        assertThat(result.getPaymentMethod()).isEqualTo("CREDIT_CARD");
    }

    @Test
    void processPayment_withAmountHigherThanFinalAmount_succeeds() {
        payment.setAmount(2000.0);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(paymentRepository.findByReservationId(1L)).thenReturn(Optional.empty());
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PaymentEntity result = paymentService.processPayment(payment);

        assertThat(result).isNotNull();
    }

    // --- Casos de error: reserva ---

    @Test
    void processPayment_whenReservationNotFound_returnsNull() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThat(paymentService.processPayment(payment)).isNull();
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void processPayment_whenReservationAlreadyConfirmed_returnsNull() {
        reservation.setStatus("CONFIRMED");
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThat(paymentService.processPayment(payment)).isNull();
    }

    @Test
    void processPayment_whenReservationCancelled_returnsNull() {
        reservation.setStatus("CANCELLED");
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThat(paymentService.processPayment(payment)).isNull();
    }

    // --- Casos de error: pago duplicado ---

    @Test
    void processPayment_whenPaymentAlreadyExists_returnsNull() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(paymentRepository.findByReservationId(1L)).thenReturn(Optional.of(payment));

        assertThat(paymentService.processPayment(payment)).isNull();
        verify(paymentRepository, never()).save(any());
    }

    // --- Casos de error: monto ---

    @Test
    void processPayment_whenAmountIsZero_returnsNull() {
        payment.setAmount(0.0);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(paymentRepository.findByReservationId(1L)).thenReturn(Optional.empty());

        assertThat(paymentService.processPayment(payment)).isNull();
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void processPayment_whenAmountIsNegative_returnsNull() {
        payment.setAmount(-100.0);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(paymentRepository.findByReservationId(1L)).thenReturn(Optional.empty());

        assertThat(paymentService.processPayment(payment)).isNull();
    }

    @Test
    void processPayment_whenAmountIsNull_returnsNull() {
        payment.setAmount(null);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(paymentRepository.findByReservationId(1L)).thenReturn(Optional.empty());

        assertThat(paymentService.processPayment(payment)).isNull();
    }

    @Test
    void processPayment_whenAmountLessThanFinalAmount_returnsNull() {
        payment.setAmount(500.0);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(paymentRepository.findByReservationId(1L)).thenReturn(Optional.empty());

        assertThat(paymentService.processPayment(payment)).isNull();
    }

    // --- Casos de error: datos de tarjeta ---

    @Test
    void processPayment_whenCardNumberIsNull_returnsNull() {
        payment.setCardNumber(null);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(paymentRepository.findByReservationId(1L)).thenReturn(Optional.empty());

        assertThat(paymentService.processPayment(payment)).isNull();
    }

    @Test
    void processPayment_whenExpirationDateIsNull_returnsNull() {
        payment.setExpirationDate(null);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(paymentRepository.findByReservationId(1L)).thenReturn(Optional.empty());

        assertThat(paymentService.processPayment(payment)).isNull();
    }

    @Test
    void processPayment_whenCvvIsNull_returnsNull() {
        payment.setCvv(null);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(paymentRepository.findByReservationId(1L)).thenReturn(Optional.empty());

        assertThat(paymentService.processPayment(payment)).isNull();
    }

    // --- getPaymentByReservationId ---

    @Test
    void getPaymentByReservationId_whenExists_returnsPayment() {
        when(paymentRepository.findByReservationId(1L)).thenReturn(Optional.of(payment));

        assertThat(paymentService.getPaymentByReservationId(1L)).isNotNull();
    }

    @Test
    void getPaymentByReservationId_whenNotFound_returnsNull() {
        when(paymentRepository.findByReservationId(99L)).thenReturn(Optional.empty());

        assertThat(paymentService.getPaymentByReservationId(99L)).isNull();
    }
}
