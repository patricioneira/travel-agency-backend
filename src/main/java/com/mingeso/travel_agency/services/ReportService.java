package com.mingeso.travel_agency.services;

import com.mingeso.travel_agency.dto.PackageRankingDTO;
import com.mingeso.travel_agency.entities.ReservationEntity;
import com.mingeso.travel_agency.repositories.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReservationRepository reservationRepository;

    // Reporte 1: Listado de ventas por período (solo CONFIRMED, sin canceladas)
    public List<ReservationEntity> getSalesReport(LocalDateTime start, LocalDateTime end) {
        return reservationRepository.findByRegistrationDateBetween(start, end)
                .stream()
                .filter(r -> "CONFIRMED".equals(r.getStatus()))
                .collect(Collectors.toList());
    }

    // Reporte 2: Ranking de paquetes vendidos con conteo, pasajeros y monto total
    public List<PackageRankingDTO> getPackageRanking(LocalDateTime start, LocalDateTime end) {
        return reservationRepository.findByRegistrationDateBetween(start, end)
                .stream()
                .filter(r -> "CONFIRMED".equals(r.getStatus()))
                .collect(Collectors.groupingBy(ReservationEntity::getPackageId))
                .entrySet().stream()
                .map(entry -> {
                    List<ReservationEntity> reservations = entry.getValue();
                    PackageRankingDTO dto = new PackageRankingDTO();
                    dto.setPackageId(entry.getKey());
                    dto.setReservationCount((long) reservations.size());
                    dto.setTotalPassengers(reservations.stream()
                            .mapToLong(r -> r.getPassengerCount() != null ? r.getPassengerCount() : 0)
                            .sum());
                    dto.setTotalAmount(reservations.stream()
                            .mapToDouble(r -> r.getFinalAmount() != null ? r.getFinalAmount() : 0.0)
                            .sum());
                    return dto;
                })
                // Ordenar por cantidad de reservas desc; en empate por monto total desc
                .sorted(Comparator.comparingLong(PackageRankingDTO::getReservationCount).reversed()
                        .thenComparing(Comparator.comparingDouble(PackageRankingDTO::getTotalAmount).reversed()))
                .collect(Collectors.toList());
    }
}
