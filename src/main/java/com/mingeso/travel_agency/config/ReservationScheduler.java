package com.mingeso.travel_agency.config;

import com.mingeso.travel_agency.entities.ReservationEntity;
import com.mingeso.travel_agency.entities.TourPackageEntity;
import com.mingeso.travel_agency.repositories.ReservationRepository;
import com.mingeso.travel_agency.repositories.TourPackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationRepository reservationRepository;
    private final TourPackageRepository tourPackageRepository;

    // Cada 30 minutos cancela reservas PENDING_PAYMENT con más de 24 horas de antigüedad
    @Scheduled(fixedRate = 1800000)
    @Transactional
    public void expireUnpaidReservations() {
        LocalDateTime expirationLimit = LocalDateTime.now().minusHours(24);

        List<ReservationEntity> expired = reservationRepository.findAll().stream()
                .filter(r -> "PENDING_PAYMENT".equals(r.getStatus()))
                .filter(r -> r.getRegistrationDate() != null
                        && r.getRegistrationDate().isBefore(expirationLimit))
                .collect(java.util.stream.Collectors.toList());
        for (ReservationEntity reservation : expired) {
            reservation.setStatus("CANCELLED");
            reservationRepository.save(reservation);

            // Restaurar cupos del paquete
            tourPackageRepository.findById(reservation.getPackageId()).ifPresent(pkg -> {
                pkg.setAvailableSlots(pkg.getAvailableSlots() + reservation.getPassengerCount());
                if (pkg.getAvailableSlots() > 0 && "SOLD_OUT".equals(pkg.getStatus())) {
                    pkg.setStatus("AVAILABLE");
                }
                tourPackageRepository.save(pkg);
            });
        }
    }
}
