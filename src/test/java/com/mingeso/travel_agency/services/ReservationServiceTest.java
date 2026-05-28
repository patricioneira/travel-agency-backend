package com.mingeso.travel_agency.services;

import com.mingeso.travel_agency.entities.PromotionEntity;
import com.mingeso.travel_agency.entities.ReservationEntity;
import com.mingeso.travel_agency.entities.TourPackageEntity;
import com.mingeso.travel_agency.repositories.PromotionRepository;
import com.mingeso.travel_agency.repositories.ReservationRepository;
import com.mingeso.travel_agency.repositories.TourPackageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private TourPackageRepository tourPackageRepository;
    @Mock private PromotionRepository promotionRepository;

    @InjectMocks
    private ReservationService reservationService;

    private TourPackageEntity pkg;
    private ReservationEntity reservation;

    @BeforeEach
    void setUp() {
        pkg = new TourPackageEntity();
        pkg.setId(1L);
        pkg.setPrice(1000.0);
        pkg.setAvailableSlots(10);
        pkg.setStatus("AVAILABLE");
        pkg.setStartDate(LocalDate.now().plusDays(5));
        pkg.setEndDate(LocalDate.now().plusDays(15));

        reservation = new ReservationEntity();
        reservation.setPackageId(1L);
        reservation.setUserId(42L);
        reservation.setPassengerCount(2);
    }

    private void mockNoDiscounts() {
        when(reservationRepository.countByUserIdAndStatus(42L, "CONFIRMED")).thenReturn(0L);
        when(reservationRepository.countByUserIdAndRegistrationDateAfter(eq(42L), any())).thenReturn(0L);
        when(promotionRepository.findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(any(), any()))
                .thenReturn(List.of());
    }

    // --- Sin descuentos ---

    @Test
    void createReservation_withNoDiscounts_calculatesCorrectly() {
        when(tourPackageRepository.findById(1L)).thenReturn(Optional.of(pkg));
        mockNoDiscounts();
        when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReservationEntity result = reservationService.createReservation(reservation);

        assertThat(result.getBaseAmount()).isEqualTo(2000.0);
        assertThat(result.getTotalDiscount()).isEqualTo(0.0);
        assertThat(result.getFinalAmount()).isEqualTo(2000.0);
        assertThat(result.getDiscountDetail()).isEqualTo("Sin descuentos");
        assertThat(result.getStatus()).isEqualTo("PENDING_PAYMENT");
    }

    // --- Descuento por grupo ---

    @Test
    void createReservation_withGroupDiscount_applies10Percent() {
        reservation.setPassengerCount(4);
        when(tourPackageRepository.findById(1L)).thenReturn(Optional.of(pkg));
        mockNoDiscounts();
        when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReservationEntity result = reservationService.createReservation(reservation);

        assertThat(result.getTotalDiscount()).isEqualTo(400.0);
        assertThat(result.getFinalAmount()).isEqualTo(3600.0);
        assertThat(result.getDiscountDetail()).contains("Descuento por grupo");
    }

    // --- Descuento por cliente frecuente ---

    @Test
    void createReservation_withFrequentClientDiscount_applies10Percent() {
        when(tourPackageRepository.findById(1L)).thenReturn(Optional.of(pkg));
        when(reservationRepository.countByUserIdAndStatus(42L, "CONFIRMED")).thenReturn(3L);
        when(reservationRepository.countByUserIdAndRegistrationDateAfter(eq(42L), any())).thenReturn(0L);
        when(promotionRepository.findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(any(), any()))
                .thenReturn(List.of());
        when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReservationEntity result = reservationService.createReservation(reservation);

        assertThat(result.getTotalDiscount()).isEqualTo(200.0);
        assertThat(result.getFinalAmount()).isEqualTo(1800.0);
        assertThat(result.getDiscountDetail()).contains("Cliente frecuente");
    }

    // --- Descuento por múltiples paquetes ---

    @Test
    void createReservation_withMultiPackageDiscount_applies5Percent() {
        when(tourPackageRepository.findById(1L)).thenReturn(Optional.of(pkg));
        when(reservationRepository.countByUserIdAndStatus(42L, "CONFIRMED")).thenReturn(0L);
        when(reservationRepository.countByUserIdAndRegistrationDateAfter(eq(42L), any())).thenReturn(1L);
        when(promotionRepository.findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(any(), any()))
                .thenReturn(List.of());
        when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReservationEntity result = reservationService.createReservation(reservation);

        assertThat(result.getTotalDiscount()).isEqualTo(100.0);
        assertThat(result.getFinalAmount()).isEqualTo(1900.0);
        assertThat(result.getDiscountDetail()).contains("múltiples paquetes");
    }

    @Test
    void createReservation_withNoRecentReservations_noMultiPackageDiscount() {
        when(tourPackageRepository.findById(1L)).thenReturn(Optional.of(pkg));
        mockNoDiscounts();
        when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReservationEntity result = reservationService.createReservation(reservation);

        assertThat(result.getDiscountDetail()).doesNotContain("múltiples paquetes");
    }

    // --- Descuento por promoción activa ---

    @Test
    void createReservation_withActivePromotion_appliesPromotionDiscount() {
        PromotionEntity promo = new PromotionEntity();
        promo.setDescription("Verano 2025");
        promo.setDiscountPercent(0.05);
        promo.setStartDate(LocalDate.now().minusDays(1));
        promo.setEndDate(LocalDate.now().plusDays(10));
        promo.setActive(true);

        when(tourPackageRepository.findById(1L)).thenReturn(Optional.of(pkg));
        when(reservationRepository.countByUserIdAndStatus(42L, "CONFIRMED")).thenReturn(0L);
        when(reservationRepository.countByUserIdAndRegistrationDateAfter(eq(42L), any())).thenReturn(0L);
        when(promotionRepository.findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(any(), any()))
                .thenReturn(List.of(promo));
        when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReservationEntity result = reservationService.createReservation(reservation);

        assertThat(result.getTotalDiscount()).isEqualTo(100.0);
        assertThat(result.getFinalAmount()).isEqualTo(1900.0);
        assertThat(result.getDiscountDetail()).contains("Verano 2025");
    }

    @Test
    void createReservation_withNoActivePromotion_noPromotionDiscount() {
        when(tourPackageRepository.findById(1L)).thenReturn(Optional.of(pkg));
        mockNoDiscounts();
        when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReservationEntity result = reservationService.createReservation(reservation);

        assertThat(result.getDiscountDetail()).doesNotContain("Promoción");
    }

    // --- Cap 20% ---

    @Test
    void createReservation_withBothDiscounts_capsAt20Percent() {
        reservation.setPassengerCount(5);
        when(tourPackageRepository.findById(1L)).thenReturn(Optional.of(pkg));
        when(reservationRepository.countByUserIdAndStatus(42L, "CONFIRMED")).thenReturn(5L);
        when(reservationRepository.countByUserIdAndRegistrationDateAfter(eq(42L), any())).thenReturn(0L);
        when(promotionRepository.findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(any(), any()))
                .thenReturn(List.of());
        when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReservationEntity result = reservationService.createReservation(reservation);

        assertThat(result.getTotalDiscount()).isEqualTo(1000.0);
        assertThat(result.getFinalAmount()).isEqualTo(4000.0);
        assertThat(result.getDiscountDetail()).contains("Descuento máximo aplicado: 20%");
    }

    @Test
    void createReservation_finalAmountNeverNegative() {
        reservation.setPassengerCount(5);
        PromotionEntity promo = new PromotionEntity();
        promo.setDiscountPercent(0.50);
        promo.setDescription("SuperDescuento");
        promo.setActive(true);

        when(tourPackageRepository.findById(1L)).thenReturn(Optional.of(pkg));
        when(reservationRepository.countByUserIdAndStatus(42L, "CONFIRMED")).thenReturn(5L);
        when(reservationRepository.countByUserIdAndRegistrationDateAfter(eq(42L), any())).thenReturn(1L);
        when(promotionRepository.findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(any(), any()))
                .thenReturn(List.of(promo));
        when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReservationEntity result = reservationService.createReservation(reservation);

        assertThat(result.getFinalAmount()).isGreaterThanOrEqualTo(0.0);
    }

    // --- Cupos y estado ---

    @Test
    void createReservation_decrementsAvailableSlots() {
        reservation.setPassengerCount(3);
        when(tourPackageRepository.findById(1L)).thenReturn(Optional.of(pkg));
        mockNoDiscounts();
        when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        reservationService.createReservation(reservation);

        assertThat(pkg.getAvailableSlots()).isEqualTo(7);
        verify(tourPackageRepository).save(pkg);
    }

    @Test
    void createReservation_whenSlotsReachZero_marksSoldOut() {
        reservation.setPassengerCount(10);
        when(tourPackageRepository.findById(1L)).thenReturn(Optional.of(pkg));
        mockNoDiscounts();
        when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        reservationService.createReservation(reservation);

        assertThat(pkg.getAvailableSlots()).isEqualTo(0);
        assertThat(pkg.getStatus()).isEqualTo("SOLD_OUT");
    }

    @Test
    void createReservation_setsRegistrationDate() {
        when(tourPackageRepository.findById(1L)).thenReturn(Optional.of(pkg));
        mockNoDiscounts();
        when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReservationEntity result = reservationService.createReservation(reservation);

        assertThat(result.getRegistrationDate()).isNotNull();
    }

    // --- Errores ---

    @Test
    void createReservation_whenPackageNotFound_throwsException() {
        when(tourPackageRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.createReservation(reservation))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Package not found");
    }

    @Test
    void createReservation_whenPackageNotAvailable_throwsException() {
        pkg.setStatus("CANCELLED");
        when(tourPackageRepository.findById(1L)).thenReturn(Optional.of(pkg));

        assertThatThrownBy(() -> reservationService.createReservation(reservation))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Package is not available");
    }

    @Test
    void createReservation_whenPackageSoldOut_throwsException() {
        pkg.setStatus("SOLD_OUT");
        when(tourPackageRepository.findById(1L)).thenReturn(Optional.of(pkg));

        assertThatThrownBy(() -> reservationService.createReservation(reservation))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Package is not available");
    }

    @Test
    void createReservation_whenPassengerCountIsZero_throwsException() {
        reservation.setPassengerCount(0);
        when(tourPackageRepository.findById(1L)).thenReturn(Optional.of(pkg));

        assertThatThrownBy(() -> reservationService.createReservation(reservation))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Passenger count must be > 0");
    }

    @Test
    void createReservation_whenNotEnoughSlots_throwsException() {
        pkg.setAvailableSlots(1);
        reservation.setPassengerCount(5);
        when(tourPackageRepository.findById(1L)).thenReturn(Optional.of(pkg));

        assertThatThrownBy(() -> reservationService.createReservation(reservation))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("No available slots");
    }

    // --- Consultas ---

    @Test
    void getReservationById_whenExists_returnsReservation() {
        reservation.setId(1L);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThat(reservationService.getReservationById(1L)).isNotNull();
    }

    @Test
    void getReservationById_whenNotFound_returnsNull() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(reservationService.getReservationById(99L)).isNull();
    }

    @Test
    void getReservationsByUser_returnsUserReservations() {
        when(reservationRepository.findByUserId(42L)).thenReturn(List.of(reservation));

        assertThat(reservationService.getReservationsByUser(42L)).hasSize(1);
    }

    @Test
    void getReservationsByUser_whenNone_returnsEmpty() {
        when(reservationRepository.findByUserId(99L)).thenReturn(List.of());

        assertThat(reservationService.getReservationsByUser(99L)).isEmpty();
    }

    @Test
    void getAllReservations_returnsAll() {
        when(reservationRepository.findAll()).thenReturn(List.of(reservation, new ReservationEntity()));

        assertThat(reservationService.getAllReservations()).hasSize(2);
    }

    // --- Cancelación ---

    @Test
    void cancelReservation_pendingPayment_cancelsAndRestoresSlots() {
        reservation.setId(1L);
        reservation.setStatus("PENDING_PAYMENT");
        reservation.setPassengerCount(3);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(tourPackageRepository.findById(1L)).thenReturn(Optional.of(pkg));
        when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReservationEntity result = reservationService.cancelReservation(1L);

        assertThat(result.getStatus()).isEqualTo("CANCELLED");
        assertThat(pkg.getAvailableSlots()).isEqualTo(13);
    }

    @Test
    void cancelReservation_whenSoldOutAndSlotsRestored_setsAvailable() {
        pkg.setAvailableSlots(0);
        pkg.setStatus("SOLD_OUT");
        reservation.setId(1L);
        reservation.setStatus("PENDING_PAYMENT");
        reservation.setPassengerCount(3);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(tourPackageRepository.findById(1L)).thenReturn(Optional.of(pkg));
        when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        reservationService.cancelReservation(1L);

        assertThat(pkg.getStatus()).isEqualTo("AVAILABLE");
    }

    @Test
    void cancelReservation_confirmedReservation_cancelsWithoutRestoringSlots() {
        reservation.setId(1L);
        reservation.setStatus("CONFIRMED");
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReservationEntity result = reservationService.cancelReservation(1L);

        assertThat(result.getStatus()).isEqualTo("CANCELLED");
        verify(tourPackageRepository, never()).findById(any());
    }

    @Test
    void cancelReservation_alreadyCancelled_returnsNull() {
        reservation.setId(1L);
        reservation.setStatus("CANCELLED");
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThat(reservationService.cancelReservation(1L)).isNull();
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void cancelReservation_whenNotFound_returnsNull() {
        when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(reservationService.cancelReservation(99L)).isNull();
    }
}
