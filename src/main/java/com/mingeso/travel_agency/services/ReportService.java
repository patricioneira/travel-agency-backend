package com.mingeso.travel_agency.services;

import com.mingeso.travel_agency.entities.ReservationEntity;
import com.mingeso.travel_agency.repositories.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReservationRepository reservationRepository;

    // Reporte 1: Listado de ventas por período
    public List<ReservationEntity> getSalesReport(LocalDateTime start, LocalDateTime end) {
        return reservationRepository.findByRegistrationDateBetween(start, end)
                .stream()
                .filter(r -> "CONFIRMED".equals(r.getStatus()))
                .collect(Collectors.toList());
    }

    // Reporte 2: Ranking de paquetes vendidos
    public Map<Long, Long> getPackageRanking(LocalDateTime start, LocalDateTime end) {
        return reservationRepository.findByRegistrationDateBetween(start, end)
                .stream()
                .filter(r -> "CONFIRMED".equals(r.getStatus()))
                .collect(Collectors.groupingBy(ReservationEntity::getPackageId, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }
}