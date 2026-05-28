package com.mingeso.travel_agency.services;

import com.mingeso.travel_agency.entities.TourPackageEntity;
import com.mingeso.travel_agency.repositories.ReservationRepository;
import com.mingeso.travel_agency.repositories.TourPackageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TourPackageServiceTest {

    @Mock private TourPackageRepository tourPackageRepository;
    @Mock private ReservationRepository reservationRepository;

    @InjectMocks
    private TourPackageService tourPackageService;

    private TourPackageEntity availablePackage;

    @BeforeEach
    void setUp() {
        availablePackage = new TourPackageEntity();
        availablePackage.setId(1L);
        availablePackage.setName("Paris Tour");
        availablePackage.setDestination("Paris");
        availablePackage.setPrice(1500.0);
        availablePackage.setTotalSlots(20);
        availablePackage.setAvailableSlots(20);
        availablePackage.setStartDate(LocalDate.now().plusDays(10));
        availablePackage.setEndDate(LocalDate.now().plusDays(20));
        availablePackage.setStatus("AVAILABLE");
    }

    // --- getAllPackages ---

    @Test
    void getAllPackages_returnsOnlyAvailableAndValid() {
        when(tourPackageRepository.findByStatusAndEndDateGreaterThanEqual(eq("AVAILABLE"), any()))
                .thenReturn(List.of(availablePackage));

        List<TourPackageEntity> result = tourPackageService.getAllPackages();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("AVAILABLE");
    }

    @Test
    void getAllPackages_excludesExpiredPackages() {
        when(tourPackageRepository.findByStatusAndEndDateGreaterThanEqual(eq("AVAILABLE"), any()))
                .thenReturn(List.of());

        assertThat(tourPackageService.getAllPackages()).isEmpty();
    }

    // --- getAllPackagesAdmin ---

    @Test
    void getAllPackagesAdmin_returnsAll() {
        TourPackageEntity cancelled = new TourPackageEntity();
        cancelled.setStatus("CANCELLED");
        when(tourPackageRepository.findAll()).thenReturn(List.of(availablePackage, cancelled));

        assertThat(tourPackageService.getAllPackagesAdmin()).hasSize(2);
    }

    // --- searchByDestination ---

    @Test
    void searchByDestination_returnsOnlyAvailableAndValid() {
        when(tourPackageRepository.findByDestinationContainingIgnoreCaseAndStatusAndEndDateGreaterThanEqual(
                eq("paris"), eq("AVAILABLE"), any())).thenReturn(List.of(availablePackage));

        List<TourPackageEntity> result = tourPackageService.searchByDestination("paris");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDestination()).isEqualTo("Paris");
    }

    // --- searchByPriceRange ---

    @Test
    void searchByPriceRange_returnsMatchingPackages() {
        when(tourPackageRepository.findByPriceBetweenAndStatusAndEndDateGreaterThanEqual(
                eq(1000.0), eq(2000.0), eq("AVAILABLE"), any()))
                .thenReturn(List.of(availablePackage));

        assertThat(tourPackageService.searchByPriceRange(1000.0, 2000.0)).hasSize(1);
    }

    // --- searchByDestinationAndPrice (búsqueda combinada) ---

    @Test
    void searchByDestinationAndPrice_returnsCombinedResults() {
        when(tourPackageRepository
                .findByDestinationContainingIgnoreCaseAndPriceBetweenAndStatusAndEndDateGreaterThanEqual(
                        eq("paris"), eq(1000.0), eq(2000.0), eq("AVAILABLE"), any()))
                .thenReturn(List.of(availablePackage));

        List<TourPackageEntity> result = tourPackageService.searchByDestinationAndPrice(
                "paris", 1000.0, 2000.0);

        assertThat(result).hasSize(1);
    }

    @Test
    void searchByDestinationAndPrice_whenNoMatch_returnsEmpty() {
        when(tourPackageRepository
                .findByDestinationContainingIgnoreCaseAndPriceBetweenAndStatusAndEndDateGreaterThanEqual(
                        any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        assertThat(tourPackageService.searchByDestinationAndPrice("mars", 1.0, 2.0)).isEmpty();
    }

    // --- getPackageById ---

    @Test
    void getPackageById_whenExists_returnsPackage() {
        when(tourPackageRepository.findById(1L)).thenReturn(Optional.of(availablePackage));

        assertThat(tourPackageService.getPackageById(1L)).isNotNull();
    }

    @Test
    void getPackageById_whenNotFound_returnsNull() {
        when(tourPackageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(tourPackageService.getPackageById(99L)).isNull();
    }

    // --- savePackage ---

    @Test
    void savePackage_withValidData_saves() {
        when(tourPackageRepository.save(any())).thenReturn(availablePackage);

        assertThat(tourPackageService.savePackage(availablePackage)).isNotNull();
    }

    @Test
    void savePackage_setsDefaultStatusAvailable() {
        availablePackage.setStatus(null);
        when(tourPackageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(tourPackageService.savePackage(availablePackage).getStatus()).isEqualTo("AVAILABLE");
    }

    @Test
    void savePackage_withZeroAvailableSlots_setsSoldOut() {
        availablePackage.setAvailableSlots(0);
        availablePackage.setStatus(null);
        when(tourPackageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(tourPackageService.savePackage(availablePackage).getStatus()).isEqualTo("SOLD_OUT");
    }

    @Test
    void savePackage_setsAvailableSlotsFromTotalWhenNull() {
        availablePackage.setAvailableSlots(null);
        when(tourPackageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(tourPackageService.savePackage(availablePackage).getAvailableSlots()).isEqualTo(20);
    }

    @Test
    void savePackage_withZeroPrice_returnsNull() {
        availablePackage.setPrice(0.0);
        assertThat(tourPackageService.savePackage(availablePackage)).isNull();
        verify(tourPackageRepository, never()).save(any());
    }

    @Test
    void savePackage_withNegativePrice_returnsNull() {
        availablePackage.setPrice(-100.0);
        assertThat(tourPackageService.savePackage(availablePackage)).isNull();
    }

    @Test
    void savePackage_withZeroSlots_returnsNull() {
        availablePackage.setTotalSlots(0);
        assertThat(tourPackageService.savePackage(availablePackage)).isNull();
    }

    @Test
    void savePackage_whenEndDateBeforeStartDate_returnsNull() {
        availablePackage.setEndDate(LocalDate.now().minusDays(5));
        assertThat(tourPackageService.savePackage(availablePackage)).isNull();
    }

    @Test
    void savePackage_whenEndDateEqualsStartDate_saves() {
        availablePackage.setEndDate(availablePackage.getStartDate());
        when(tourPackageRepository.save(any())).thenReturn(availablePackage);
        assertThat(tourPackageService.savePackage(availablePackage)).isNotNull();
    }

    // --- updatePackage ---

    @Test
    void updatePackage_withNoReservations_updatesAllFields() {
        TourPackageEntity updates = new TourPackageEntity();
        updates.setName("Rome Tour");
        updates.setPrice(2000.0);
        updates.setStartDate(LocalDate.now().plusDays(5));

        when(tourPackageRepository.findById(1L)).thenReturn(Optional.of(availablePackage));
        when(reservationRepository.countByPackageId(1L)).thenReturn(0L);
        when(tourPackageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TourPackageEntity result = tourPackageService.updatePackage(1L, updates);

        assertThat(result.getName()).isEqualTo("Rome Tour");
        assertThat(result.getPrice()).isEqualTo(2000.0);
        assertThat(result.getStartDate()).isEqualTo(LocalDate.now().plusDays(5));
    }

    @Test
    void updatePackage_withReservations_doesNotChangeCriticalFields() {
        TourPackageEntity updates = new TourPackageEntity();
        updates.setPrice(9999.0);
        updates.setName("New Name");

        when(tourPackageRepository.findById(1L)).thenReturn(Optional.of(availablePackage));
        when(reservationRepository.countByPackageId(1L)).thenReturn(3L);
        when(tourPackageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TourPackageEntity result = tourPackageService.updatePackage(1L, updates);

        // Nombre se puede cambiar, precio no
        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getPrice()).isEqualTo(1500.0);
    }

    @Test
    void updatePackage_whenNotFound_returnsNull() {
        when(tourPackageRepository.findById(99L)).thenReturn(Optional.empty());
        assertThat(tourPackageService.updatePackage(99L, new TourPackageEntity())).isNull();
    }

    // --- updateStatus ---

    @Test
    void updateStatus_toCancelled_succeeds() {
        when(tourPackageRepository.findById(1L)).thenReturn(Optional.of(availablePackage));
        when(tourPackageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(tourPackageService.updateStatus(1L, "CANCELLED").getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    void updateStatus_toAvailableWithZeroSlots_returnsNull() {
        availablePackage.setAvailableSlots(0);
        when(tourPackageRepository.findById(1L)).thenReturn(Optional.of(availablePackage));

        assertThat(tourPackageService.updateStatus(1L, "AVAILABLE")).isNull();
        verify(tourPackageRepository, never()).save(any());
    }

    @Test
    void updateStatus_toSoldOut_works() {
        when(tourPackageRepository.findById(1L)).thenReturn(Optional.of(availablePackage));
        when(tourPackageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(tourPackageService.updateStatus(1L, "SOLD_OUT").getStatus()).isEqualTo("SOLD_OUT");
    }

    @Test
    void updateStatus_whenNotFound_returnsNull() {
        when(tourPackageRepository.findById(99L)).thenReturn(Optional.empty());
        assertThat(tourPackageService.updateStatus(99L, "CANCELLED")).isNull();
    }
}
