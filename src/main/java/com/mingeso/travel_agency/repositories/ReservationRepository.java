package com.mingeso.travel_agency.repositories;

import com.mingeso.travel_agency.entities.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {
    long countByUserIdAndStatus(Long userId, String status);
    List<ReservationEntity> findByRegistrationDateBetween(LocalDateTime start, LocalDateTime end);
}