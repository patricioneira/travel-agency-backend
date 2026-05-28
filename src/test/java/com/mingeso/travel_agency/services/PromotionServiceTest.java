package com.mingeso.travel_agency.services;

import com.mingeso.travel_agency.entities.PromotionEntity;
import com.mingeso.travel_agency.repositories.PromotionRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    @Mock
    private PromotionRepository promotionRepository;

    @InjectMocks
    private PromotionService promotionService;

    private PromotionEntity validPromotion;

    @BeforeEach
    void setUp() {
        validPromotion = new PromotionEntity();
        validPromotion.setId(1L);
        validPromotion.setDescription("Verano 2025");
        validPromotion.setDiscountPercent(0.05);
        validPromotion.setStartDate(LocalDate.now());
        validPromotion.setEndDate(LocalDate.now().plusDays(30));
        validPromotion.setActive(true);
    }

    // --- getAllPromotions ---

    @Test
    void getAllPromotions_returnsAll() {
        when(promotionRepository.findAll()).thenReturn(List.of(validPromotion));

        assertThat(promotionService.getAllPromotions()).hasSize(1);
    }

    // --- getActivePromotions ---

    @Test
    void getActivePromotions_returnsOnlyActive() {
        when(promotionRepository.findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(any(), any()))
                .thenReturn(List.of(validPromotion));

        List<PromotionEntity> result = promotionService.getActivePromotions();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isActive()).isTrue();
    }

    @Test
    void getActivePromotions_whenNone_returnsEmpty() {
        when(promotionRepository.findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(any(), any()))
                .thenReturn(List.of());

        assertThat(promotionService.getActivePromotions()).isEmpty();
    }

    // --- savePromotion ---

    @Test
    void savePromotion_withValidData_saves() {
        when(promotionRepository.save(any())).thenReturn(validPromotion);

        PromotionEntity result = promotionService.savePromotion(validPromotion);

        assertThat(result).isNotNull();
        verify(promotionRepository).save(validPromotion);
    }

    @Test
    void savePromotion_withZeroDiscount_returnsNull() {
        validPromotion.setDiscountPercent(0.0);

        assertThat(promotionService.savePromotion(validPromotion)).isNull();
        verify(promotionRepository, never()).save(any());
    }

    @Test
    void savePromotion_withNegativeDiscount_returnsNull() {
        validPromotion.setDiscountPercent(-0.05);

        assertThat(promotionService.savePromotion(validPromotion)).isNull();
    }

    @Test
    void savePromotion_withNullStartDate_returnsNull() {
        validPromotion.setStartDate(null);

        assertThat(promotionService.savePromotion(validPromotion)).isNull();
    }

    @Test
    void savePromotion_withNullEndDate_returnsNull() {
        validPromotion.setEndDate(null);

        assertThat(promotionService.savePromotion(validPromotion)).isNull();
    }

    @Test
    void savePromotion_whenEndDateBeforeStartDate_returnsNull() {
        validPromotion.setEndDate(LocalDate.now().minusDays(5));

        assertThat(promotionService.savePromotion(validPromotion)).isNull();
    }

    // --- updatePromotion ---

    @Test
    void updatePromotion_withValidId_updatesFields() {
        PromotionEntity updates = new PromotionEntity();
        updates.setDescription("Invierno 2025");
        updates.setDiscountPercent(0.10);

        when(promotionRepository.findById(1L)).thenReturn(Optional.of(validPromotion));
        when(promotionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PromotionEntity result = promotionService.updatePromotion(1L, updates);

        assertThat(result.getDescription()).isEqualTo("Invierno 2025");
        assertThat(result.getDiscountPercent()).isEqualTo(0.10);
    }

    @Test
    void updatePromotion_whenNotFound_returnsNull() {
        when(promotionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(promotionService.updatePromotion(99L, new PromotionEntity())).isNull();
    }

    // --- deactivatePromotion ---

    @Test
    void deactivatePromotion_setsActiveFalse() {
        when(promotionRepository.findById(1L)).thenReturn(Optional.of(validPromotion));

        promotionService.deactivatePromotion(1L);

        assertThat(validPromotion.isActive()).isFalse();
        verify(promotionRepository).save(validPromotion);
    }

    @Test
    void deactivatePromotion_whenNotFound_doesNothing() {
        when(promotionRepository.findById(99L)).thenReturn(Optional.empty());

        promotionService.deactivatePromotion(99L);

        verify(promotionRepository, never()).save(any());
    }
}
