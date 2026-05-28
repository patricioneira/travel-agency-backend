package com.mingeso.travel_agency.repositories;

import com.mingeso.travel_agency.entities.PromotionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<PromotionEntity, Long> {

    // Retorna promociones activas cuyo rango de fechas incluye la fecha dada
    List<PromotionEntity> findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            LocalDate startDate, LocalDate endDate);
}
