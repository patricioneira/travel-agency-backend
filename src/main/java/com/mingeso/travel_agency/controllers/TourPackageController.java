package com.mingeso.travel_agency.controllers;

import com.mingeso.travel_agency.entities.TourPackageEntity;
import com.mingeso.travel_agency.services.TourPackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/packages")
@RequiredArgsConstructor
@CrossOrigin("*")
public class TourPackageController {
    private final TourPackageService tourPackageService;

    // Clientes ven solo paquetes AVAILABLE y vigentes
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/")
    public ResponseEntity<List<TourPackageEntity>> getAllPackages() {
        return ResponseEntity.ok(tourPackageService.getAllPackages());
    }

    // Admin ve todos sin importar estado
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<List<TourPackageEntity>> getAllPackagesAdmin() {
        return ResponseEntity.ok(tourPackageService.getAllPackagesAdmin());
    }

    // Búsqueda con filtros opcionales: destino, precio o ambos combinados
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<List<TourPackageEntity>> search(
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {

        boolean hasDestination = destination != null && !destination.isBlank();
        boolean hasPrice = minPrice != null && maxPrice != null;

        if (hasDestination && hasPrice) {
            return ResponseEntity.ok(
                    tourPackageService.searchByDestinationAndPrice(destination, minPrice, maxPrice));
        }
        if (hasDestination) {
            return ResponseEntity.ok(tourPackageService.searchByDestination(destination));
        }
        if (hasPrice) {
            return ResponseEntity.ok(tourPackageService.searchByPriceRange(minPrice, maxPrice));
        }
        return ResponseEntity.ok(tourPackageService.getAllPackages());
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<TourPackageEntity> getPackageById(@PathVariable Long id) {
        TourPackageEntity tourPackage = tourPackageService.getPackageById(id);
        if (tourPackage == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(tourPackage);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/")
    public ResponseEntity<TourPackageEntity> savePackage(@RequestBody TourPackageEntity tourPackage) {
        TourPackageEntity saved = tourPackageService.savePackage(tourPackage);
        if (saved == null) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(saved);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<TourPackageEntity> updatePackage(@PathVariable Long id,
                                                           @RequestBody TourPackageEntity updatedData) {
        TourPackageEntity updated = tourPackageService.updatePackage(id, updatedData);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    // Cambiar estado: AVAILABLE, SOLD_OUT, NOT_VALID, CANCELLED
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<TourPackageEntity> updateStatus(@PathVariable Long id,
                                                          @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        if (newStatus == null) return ResponseEntity.badRequest().build();
        TourPackageEntity updated = tourPackageService.updateStatus(id, newStatus);
        if (updated == null) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(updated);
    }
}
