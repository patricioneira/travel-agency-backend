package com.mingeso.travel_agency.repositories;

import com.mingeso.travel_agency.entities.TourPackageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TourPackageRepository extends JpaRepository<TourPackageEntity, Long> {
    List<TourPackageEntity> findByDestinationContainingIgnoreCase(String destination);
    List<TourPackageEntity> findByPriceBetween(Double minPrice, Double maxPrice);
}