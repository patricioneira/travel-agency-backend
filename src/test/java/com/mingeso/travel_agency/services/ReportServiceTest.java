package com.mingeso.travel_agency.services;

import com.mingeso.travel_agency.dto.PackageRankingDTO;
import com.mingeso.travel_agency.entities.ReservationEntity;
import com.mingeso.travel_agency.repositories.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReportService reportService;

    private LocalDateTime start;
    private LocalDateTime end;

    private ReservationEntity confirmed(Long id, Long packageId, int passengers, double amount) {
        ReservationEntity r = new ReservationEntity();
        r.setId(id);
        r.setPackageId(packageId);
        r.setPassengerCount(passengers);
        r.setFinalAmount(amount);
        r.setStatus("CONFIRMED");
        return r;
    }

    private ReservationEntity pending(Long id, Long packageId) {
        ReservationEntity r = new ReservationEntity();
        r.setId(id);
        r.setPackageId(packageId);
        r.setPassengerCount(2);
        r.setFinalAmount(1000.0);
        r.setStatus("PENDING_PAYMENT");
        return r;
    }

    private ReservationEntity cancelled(Long id, Long packageId) {
        ReservationEntity r = new ReservationEntity();
        r.setId(id);
        r.setPackageId(packageId);
        r.setStatus("CANCELLED");
        return r;
    }

    @BeforeEach
    void setUp() {
        start = LocalDateTime.of(2025, 1, 1, 0, 0);
        end   = LocalDateTime.of(2025, 12, 31, 23, 59);
    }

    // --- getSalesReport ---

    @Test
    void getSalesReport_returnsOnlyConfirmedReservations() {
        List<ReservationEntity> data = List.of(
                confirmed(1L, 10L, 2, 2000.0),
                pending(2L, 10L),
                confirmed(3L, 20L, 1, 1500.0)
        );
        when(reservationRepository.findByRegistrationDateBetween(start, end)).thenReturn(data);

        List<ReservationEntity> result = reportService.getSalesReport(start, end);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(r -> "CONFIRMED".equals(r.getStatus()));
    }

    @Test
    void getSalesReport_excludesCancelledReservations() {
        List<ReservationEntity> data = List.of(
                confirmed(1L, 10L, 2, 2000.0),
                cancelled(2L, 10L)
        );
        when(reservationRepository.findByRegistrationDateBetween(start, end)).thenReturn(data);

        List<ReservationEntity> result = reportService.getSalesReport(start, end);

        assertThat(result).hasSize(1);
    }

    @Test
    void getSalesReport_whenNoConfirmed_returnsEmpty() {
        when(reservationRepository.findByRegistrationDateBetween(start, end))
                .thenReturn(List.of(pending(1L, 10L)));

        assertThat(reportService.getSalesReport(start, end)).isEmpty();
    }

    @Test
    void getSalesReport_whenRepositoryEmpty_returnsEmpty() {
        when(reservationRepository.findByRegistrationDateBetween(start, end)).thenReturn(List.of());

        assertThat(reportService.getSalesReport(start, end)).isEmpty();
    }

    @Test
    void getSalesReport_usesCorrectDateRange() {
        when(reservationRepository.findByRegistrationDateBetween(start, end)).thenReturn(List.of());

        reportService.getSalesReport(start, end);

        verify(reservationRepository).findByRegistrationDateBetween(start, end);
    }

    // --- getPackageRanking ---

    @Test
    void getPackageRanking_groupsByPackageAndCountsCorrectly() {
        List<ReservationEntity> data = List.of(
                confirmed(1L, 10L, 2, 2000.0),
                confirmed(2L, 10L, 3, 3000.0),
                confirmed(3L, 20L, 1, 1000.0)
        );
        when(reservationRepository.findByRegistrationDateBetween(start, end)).thenReturn(data);

        List<PackageRankingDTO> result = reportService.getPackageRanking(start, end);

        assertThat(result).hasSize(2);
        PackageRankingDTO top = result.get(0);
        assertThat(top.getPackageId()).isEqualTo(10L);
        assertThat(top.getReservationCount()).isEqualTo(2L);
    }

    @Test
    void getPackageRanking_calculatesTotalPassengersCorrectly() {
        List<ReservationEntity> data = List.of(
                confirmed(1L, 10L, 2, 2000.0),
                confirmed(2L, 10L, 3, 3000.0)
        );
        when(reservationRepository.findByRegistrationDateBetween(start, end)).thenReturn(data);

        List<PackageRankingDTO> result = reportService.getPackageRanking(start, end);

        assertThat(result.get(0).getTotalPassengers()).isEqualTo(5L);
    }

    @Test
    void getPackageRanking_calculatesTotalAmountCorrectly() {
        List<ReservationEntity> data = List.of(
                confirmed(1L, 10L, 2, 2000.0),
                confirmed(2L, 10L, 3, 3000.0)
        );
        when(reservationRepository.findByRegistrationDateBetween(start, end)).thenReturn(data);

        List<PackageRankingDTO> result = reportService.getPackageRanking(start, end);

        assertThat(result.get(0).getTotalAmount()).isEqualTo(5000.0);
    }

    @Test
    void getPackageRanking_isOrderedDescendingByReservationCount() {
        List<ReservationEntity> data = List.of(
                confirmed(1L, 30L, 1, 1000.0),
                confirmed(2L, 10L, 2, 2000.0),
                confirmed(3L, 10L, 2, 2000.0),
                confirmed(4L, 20L, 3, 3000.0),
                confirmed(5L, 20L, 3, 3000.0),
                confirmed(6L, 20L, 3, 3000.0)
        );
        when(reservationRepository.findByRegistrationDateBetween(start, end)).thenReturn(data);

        List<PackageRankingDTO> result = reportService.getPackageRanking(start, end);

        assertThat(result.get(0).getPackageId()).isEqualTo(20L);
        assertThat(result.get(1).getPackageId()).isEqualTo(10L);
        assertThat(result.get(2).getPackageId()).isEqualTo(30L);
    }

    @Test
    void getPackageRanking_excludesCancelledReservations() {
        List<ReservationEntity> data = List.of(
                confirmed(1L, 10L, 2, 2000.0),
                cancelled(2L, 10L)
        );
        when(reservationRepository.findByRegistrationDateBetween(start, end)).thenReturn(data);

        List<PackageRankingDTO> result = reportService.getPackageRanking(start, end);

        assertThat(result.get(0).getReservationCount()).isEqualTo(1L);
    }

    @Test
    void getPackageRanking_excludesPendingReservations() {
        List<ReservationEntity> data = List.of(
                confirmed(1L, 10L, 2, 2000.0),
                pending(2L, 10L)
        );
        when(reservationRepository.findByRegistrationDateBetween(start, end)).thenReturn(data);

        List<PackageRankingDTO> result = reportService.getPackageRanking(start, end);

        assertThat(result.get(0).getReservationCount()).isEqualTo(1L);
    }

    @Test
    void getPackageRanking_whenNoConfirmed_returnsEmpty() {
        when(reservationRepository.findByRegistrationDateBetween(start, end))
                .thenReturn(List.of(pending(1L, 10L)));

        assertThat(reportService.getPackageRanking(start, end)).isEmpty();
    }

    @Test
    void getPackageRanking_whenRepositoryEmpty_returnsEmpty() {
        when(reservationRepository.findByRegistrationDateBetween(start, end)).thenReturn(List.of());

        assertThat(reportService.getPackageRanking(start, end)).isEmpty();
    }
}
