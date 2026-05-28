package com.mingeso.travel_agency.services;

import com.mingeso.travel_agency.entities.ReservationEntity;
import com.mingeso.travel_agency.entities.TourPackageEntity;
import com.mingeso.travel_agency.repositories.ReservationRepository;
import com.mingeso.travel_agency.repositories.TourPackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final TourPackageRepository tourPackageRepository;

    @Transactional
    public ReservationEntity createReservation(ReservationEntity reservation) {
        TourPackageEntity pkg = tourPackageRepository.findById(reservation.getPackageId())
                .orElseThrow(() -> new RuntimeException("Package not found"));

        // Validaciones Operativas [7, 11]
        if (pkg.getAvailableSlots() < reservation.getPassengerCount()) {
            throw new RuntimeException("No available slots");
        }
        if (reservation.getPassengerCount() <= 0) {
            throw new RuntimeException("Passenger count must be > 0");
        }

        // Lógica de Negocio (Descuentos) [8-10]
        double baseAmount = pkg.getPrice() * reservation.getPassengerCount();
        double discountPercent = 0.0;

        // 1. Descuento por Grupo (>= 4 personas: 10%)
        if (reservation.getPassengerCount() >= 4) {
            discountPercent += 0.10;
        }

        // 2. Descuento Cliente Frecuente (>= 3 reservas PAGADAS) [8]
        long pastPaidReservations = reservationRepository.countByUserIdAndStatus(reservation.getUserId(), "CONFIRMED");
        if (pastPaidReservations >= 3) {
            discountPercent += 0.10;
        }

        // 3. Tope de Descuento (Máximo 20%) [10]
        if (discountPercent > 0.20) {
            discountPercent = 0.20;
        }

        double totalDiscount = baseAmount * discountPercent;
        reservation.setBaseAmount(baseAmount);
        reservation.setTotalDiscount(totalDiscount);
        reservation.setFinalAmount(baseAmount - totalDiscount);
        reservation.setStatus("PENDING_PAYMENT");
        reservation.setRegistrationDate(LocalDateTime.now());

        // Descontar inventario [11]
        pkg.setAvailableSlots(pkg.getAvailableSlots() - reservation.getPassengerCount());
        tourPackageRepository.save(pkg);

        return reservationRepository.save(reservation);
    }
}