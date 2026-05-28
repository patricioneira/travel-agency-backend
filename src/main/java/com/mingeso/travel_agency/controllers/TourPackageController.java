package com.mingeso.travel_agency.controllers;

import com.mingeso.travel_agency.entities.TourPackageEntity;
import com.mingeso.travel_agency.services.TourPackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Importante para seguridad [1]
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/packages")
@RequiredArgsConstructor
@CrossOrigin("*")
public class TourPackageController {
    private final TourPackageService tourPackageService;

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/")
    public ResponseEntity<List<TourPackageEntity>> getAllPackages() {
        return ResponseEntity.ok(tourPackageService.getAllPackages());
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<List<TourPackageEntity>> searchByDestination(@RequestParam String destination) {
        return ResponseEntity.ok(tourPackageService.searchByDestination(destination));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<TourPackageEntity> getPackageById(@PathVariable Long id) {
        TourPackageEntity tourPackage = tourPackageService.getPackageById(id);
        if (tourPackage == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(tourPackage);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/")
    public ResponseEntity<TourPackageEntity> savePackage(@RequestBody TourPackageEntity tourPackage) {
        TourPackageEntity savedPackage = tourPackageService.savePackage(tourPackage);
        if (savedPackage == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(savedPackage);
    }
}