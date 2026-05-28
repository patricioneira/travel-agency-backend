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

        if (pkg.getAvailableSlots() < reservation.getPassengerCount()) {
            throw new RuntimeException("No available slots");
        }
        if (reservation.getPassengerCount() <= 0) {
            throw new RuntimeException("Passenger count must be > 0");
        }

        double baseAmount = pkg.getPrice() * reservation.getPassengerCount();
        double discountPercent = 0.0;

        if (reservation.getPassengerCount() >= 4) {
            discountPercent += 0.10;
        }

        long pastPaidReservations = reservationRepository.countByUserIdAndStatus(reservation.getUserId(), "CONFIRMED");
        if (pastPaidReservations >= 3) {
            discountPercent += 0.10;
        }

        if (discountPercent > 0.20) {
            discountPercent = 0.20;
        }

        double totalDiscount = baseAmount * discountPercent;
        reservation.setBaseAmount(baseAmount);
        reservation.setTotalDiscount(totalDiscount);
        reservation.setFinalAmount(baseAmount - totalDiscount);
        reservation.setStatus("PENDING_PAYMENT");
        reservation.setRegistrationDate(LocalDateTime.now());

        pkg.setAvailableSlots(pkg.getAvailableSlots() - reservation.getPassengerCount());
        tourPackageRepository.save(pkg);

        return reservationRepository.save(reservation);
    }
}