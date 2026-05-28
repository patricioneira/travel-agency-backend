package com.mingeso.travel_agency.controllers;

import com.mingeso.travel_agency.entities.PromotionEntity;
import com.mingeso.travel_agency.services.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/promotions")
@RequiredArgsConstructor
@CrossOrigin("*")
public class PromotionController {
    private final PromotionService promotionService;

    // Cualquier usuario autenticado puede ver las promociones activas
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/active")
    public ResponseEntity<List<PromotionEntity>> getActivePromotions() {
        return ResponseEntity.ok(promotionService.getActivePromotions());
    }

    // Solo ADMIN puede ver todas y gestionar
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/")
    public ResponseEntity<List<PromotionEntity>> getAllPromotions() {
        return ResponseEntity.ok(promotionService.getAllPromotions());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/")
    public ResponseEntity<PromotionEntity> createPromotion(@RequestBody PromotionEntity promotion) {
        PromotionEntity saved = promotionService.savePromotion(promotion);
        if (saved == null) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(saved);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<PromotionEntity> updatePromotion(@PathVariable Long id,
                                                           @RequestBody PromotionEntity updatedData) {
        PromotionEntity updated = promotionService.updatePromotion(id, updatedData);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivatePromotion(@PathVariable Long id) {
        promotionService.deactivatePromotion(id);
        return ResponseEntity.noContent().build();
    }
}
