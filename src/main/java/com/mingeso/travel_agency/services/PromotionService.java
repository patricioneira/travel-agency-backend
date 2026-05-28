package com.mingeso.travel_agency.services;

import com.mingeso.travel_agency.entities.PromotionEntity;
import com.mingeso.travel_agency.repositories.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PromotionService {
    private final PromotionRepository promotionRepository;

    public List<PromotionEntity> getAllPromotions() {
        return promotionRepository.findAll();
    }

    public List<PromotionEntity> getActivePromotions() {
        return promotionRepository
                .findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        LocalDate.now(), LocalDate.now());
    }

    public PromotionEntity savePromotion(PromotionEntity promotion) {
        if (promotion.getDiscountPercent() == null || promotion.getDiscountPercent() <= 0) {
            return null;
        }
        if (promotion.getStartDate() == null || promotion.getEndDate() == null) {
            return null;
        }
        if (promotion.getEndDate().isBefore(promotion.getStartDate())) {
            return null;
        }
        return promotionRepository.save(promotion);
    }

    public PromotionEntity updatePromotion(Long id, PromotionEntity updatedData) {
        Optional<PromotionEntity> existing = promotionRepository.findById(id);
        if (existing.isEmpty()) return null;

        PromotionEntity promotion = existing.get();
        if (updatedData.getDescription() != null) promotion.setDescription(updatedData.getDescription());
        if (updatedData.getDiscountPercent() != null && updatedData.getDiscountPercent() > 0)
            promotion.setDiscountPercent(updatedData.getDiscountPercent());
        if (updatedData.getStartDate() != null) promotion.setStartDate(updatedData.getStartDate());
        if (updatedData.getEndDate() != null) promotion.setEndDate(updatedData.getEndDate());
        return promotionRepository.save(promotion);
    }

    public void deactivatePromotion(Long id) {
        promotionRepository.findById(id).ifPresent(p -> {
            p.setActive(false);
            promotionRepository.save(p);
        });
    }
}
