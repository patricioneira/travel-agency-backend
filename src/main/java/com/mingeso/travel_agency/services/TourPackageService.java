package com.mingeso.travel_agency.services;

import com.mingeso.travel_agency.entities.TourPackageEntity;
import com.mingeso.travel_agency.repositories.ReservationRepository;
import com.mingeso.travel_agency.repositories.TourPackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TourPackageService {
    private final TourPackageRepository tourPackageRepository;
    private final ReservationRepository reservationRepository;

    // Solo paquetes AVAILABLE y con fechas vigentes (endDate >= hoy)
    public List<TourPackageEntity> getAllPackages() {
        return tourPackageRepository.findByStatusAndEndDateGreaterThanEqual("AVAILABLE", LocalDate.now());
    }

    // Admin ve todos sin importar estado ni fecha
    public List<TourPackageEntity> getAllPackagesAdmin() {
        return tourPackageRepository.findAll();
    }

    // Búsqueda por destino: solo AVAILABLE y vigentes
    public List<TourPackageEntity> searchByDestination(String destination) {
        return tourPackageRepository
                .findByDestinationContainingIgnoreCaseAndStatusAndEndDateGreaterThanEqual(
                        destination, "AVAILABLE", LocalDate.now());
    }

    // Búsqueda por precio: solo AVAILABLE y vigentes
    public List<TourPackageEntity> searchByPriceRange(Double minPrice, Double maxPrice) {
        return tourPackageRepository.findByPriceBetweenAndStatusAndEndDateGreaterThanEqual(
                minPrice, maxPrice, "AVAILABLE", LocalDate.now());
    }

    // Búsqueda combinada destino + precio
    public List<TourPackageEntity> searchByDestinationAndPrice(String destination,
                                                                Double minPrice,
                                                                Double maxPrice) {
        return tourPackageRepository
                .findByDestinationContainingIgnoreCaseAndPriceBetweenAndStatusAndEndDateGreaterThanEqual(
                        destination, minPrice, maxPrice, "AVAILABLE", LocalDate.now());
    }

    public TourPackageEntity getPackageById(Long id) {
        return tourPackageRepository.findById(id).orElse(null);
    }

    public TourPackageEntity savePackage(TourPackageEntity tourPackage) {
        if (tourPackage.getPrice() == null || tourPackage.getPrice() <= 0) return null;
        if (tourPackage.getTotalSlots() == null || tourPackage.getTotalSlots() <= 0) return null;
        if (tourPackage.getEndDate().isBefore(tourPackage.getStartDate())) return null;

        if (tourPackage.getAvailableSlots() == null) {
            tourPackage.setAvailableSlots(tourPackage.getTotalSlots());
        }

        // Un paquete sin cupos no puede publicarse como AVAILABLE
        if (tourPackage.getAvailableSlots() == 0) {
            tourPackage.setStatus("SOLD_OUT");
        } else if (tourPackage.getStatus() == null) {
            tourPackage.setStatus("AVAILABLE");
        }

        return tourPackageRepository.save(tourPackage);
    }

    public TourPackageEntity updatePackage(Long id, TourPackageEntity updatedData) {
        Optional<TourPackageEntity> existing = tourPackageRepository.findById(id);
        if (existing.isEmpty()) return null;

        TourPackageEntity pkg = existing.get();

        // Si hay reservas asociadas, no se pueden modificar campos críticos
        long reservationCount = reservationRepository.countByPackageId(pkg.getId());
        boolean hasReservations = reservationCount > 0;

        if (updatedData.getName() != null) pkg.setName(updatedData.getName());
        if (updatedData.getDescription() != null) pkg.setDescription(updatedData.getDescription());
        if (updatedData.getDestination() != null) pkg.setDestination(updatedData.getDestination());
        if (updatedData.getServices() != null) pkg.setServices(updatedData.getServices());
        if (updatedData.getConditions() != null) pkg.setConditions(updatedData.getConditions());
        if (updatedData.getRestrictions() != null) pkg.setRestrictions(updatedData.getRestrictions());
        if (updatedData.getTravelType() != null) pkg.setTravelType(updatedData.getTravelType());
        if (updatedData.getSeason() != null) pkg.setSeason(updatedData.getSeason());
        if (updatedData.getCategory() != null) pkg.setCategory(updatedData.getCategory());

        // Campos críticos: solo modificables si no hay reservas
        if (!hasReservations) {
            if (updatedData.getPrice() != null && updatedData.getPrice() > 0)
                pkg.setPrice(updatedData.getPrice());
            if (updatedData.getStartDate() != null) pkg.setStartDate(updatedData.getStartDate());
            if (updatedData.getEndDate() != null) pkg.setEndDate(updatedData.getEndDate());
            if (updatedData.getTotalSlots() != null && updatedData.getTotalSlots() > 0)
                pkg.setTotalSlots(updatedData.getTotalSlots());
        }

        return tourPackageRepository.save(pkg);
    }

    // Estados: AVAILABLE, SOLD_OUT, NOT_VALID, CANCELLED
    public TourPackageEntity updateStatus(Long id, String newStatus) {
        Optional<TourPackageEntity> existing = tourPackageRepository.findById(id);
        if (existing.isEmpty()) return null;

        TourPackageEntity pkg = existing.get();

        // No se puede publicar como AVAILABLE si no tiene cupos
        if ("AVAILABLE".equals(newStatus) && pkg.getAvailableSlots() == 0) {
            return null;
        }

        pkg.setStatus(newStatus);
        return tourPackageRepository.save(pkg);
    }
}
