package com.mingeso.travel_agency.services;

import com.mingeso.travel_agency.entities.TourPackageEntity;
import com.mingeso.travel_agency.repositories.TourPackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TourPackageService {
    private final TourPackageRepository tourPackageRepository;

    public List<TourPackageEntity> getAllPackages() {
        return tourPackageRepository.findAll();
    }

    public List<TourPackageEntity> searchByDestination(String destination) {
        return tourPackageRepository.findByDestinationContainingIgnoreCase(destination);
    }

    public TourPackageEntity getPackageById(Long id) {
        return tourPackageRepository.findById(id).orElse(null);
    }

    public TourPackageEntity savePackage(TourPackageEntity tourPackage) {
        if (tourPackage.getPrice() <= 0 || tourPackage.getTotalSlots() <= 0) {
            return null;
        }
        if (tourPackage.getEndDate().isBefore(tourPackage.getStartDate())) {
            return null;
        }
        return tourPackageRepository.save(tourPackage);
    }
}