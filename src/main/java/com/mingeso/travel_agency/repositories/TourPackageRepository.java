package com.mingeso.travel_agency.repositories;

import com.mingeso.travel_agency.entities.TourPackageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TourPackageRepository extends JpaRepository<TourPackageEntity, Long> {

    List<TourPackageEntity> findByStatus(String status);

    // Solo paquetes vigentes (endDate >= hoy)
    List<TourPackageEntity> findByStatusAndEndDateGreaterThanEqual(String status, LocalDate today);

    List<TourPackageEntity> findByDestinationContainingIgnoreCaseAndStatusAndEndDateGreaterThanEqual(
            String destination, String status, LocalDate today);

    List<TourPackageEntity> findByPriceBetweenAndStatusAndEndDateGreaterThanEqual(
            Double minPrice, Double maxPrice, String status, LocalDate today);

    // Búsqueda combinada destino + precio
    List<TourPackageEntity> findByDestinationContainingIgnoreCaseAndPriceBetweenAndStatusAndEndDateGreaterThanEqual(
            String destination, Double minPrice, Double maxPrice, String status, LocalDate today);

    // Métodos sin filtro de fecha (para admin)
    List<TourPackageEntity> findByDestinationContainingIgnoreCaseAndStatus(String destination, String status);

    List<TourPackageEntity> findByPriceBetween(Double minPrice, Double maxPrice);

    List<TourPackageEntity> findByPriceBetweenAndStatus(Double minPrice, Double maxPrice, String status);
}
