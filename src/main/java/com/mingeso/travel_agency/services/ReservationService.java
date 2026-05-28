package com.mingeso.travel_agency.services;

import com.mingeso.travel_agency.entities.PromotionEntity;
import com.mingeso.travel_agency.entities.ReservationEntity;
import com.mingeso.travel_agency.entities.TourPackageEntity;
import com.mingeso.travel_agency.repositories.PromotionRepository;
import com.mingeso.travel_agency.repositories.ReservationRepository;
import com.mingeso.travel_agency.repositories.TourPackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final TourPackageRepository tourPackageRepository;
    private final PromotionRepository promotionRepository;

    @Transactional
    public ReservationEntity createReservation(ReservationEntity reservation) {
        TourPackageEntity pkg = tourPackageRepository.findById(reservation.getPackageId())
                .orElseThrow(() -> new RuntimeException("Package not found"));

        if (!"AVAILABLE".equals(pkg.getStatus())) {
            throw new RuntimeException("Package is not available");
        }
        if (reservation.getPassengerCount() <= 0) {
            throw new RuntimeException("Passenger count must be > 0");
        }
        if (pkg.getAvailableSlots() < reservation.getPassengerCount()) {
            throw new RuntimeException("No available slots");
        }

        double baseAmount = pkg.getPrice() * reservation.getPassengerCount();
        double discountPercent = 0.0;
        List<String> discountDetails = new ArrayList<>();

        // Descuento por grupo (≥4 personas)
        if (reservation.getPassengerCount() >= 4) {
            discountPercent += 0.10;
            discountDetails.add("Descuento por grupo: 10%");
        }

        // Descuento por cliente frecuente (≥3 reservas confirmadas)
        long pastPaidReservations = reservationRepository.countByUserIdAndStatus(
                reservation.getUserId(), "CONFIRMED");
        if (pastPaidReservations >= 3) {
            discountPercent += 0.10;
            discountDetails.add("Cliente frecuente: 10%");
        }

        // Descuento por múltiples paquetes (otra reserva en las últimas 24 horas)
        long recentReservations = reservationRepository.countByUserIdAndRegistrationDateAfter(
                reservation.getUserId(), LocalDateTime.now().minusHours(24));
        if (recentReservations >= 1) {
            discountPercent += 0.05;
            discountDetails.add("Descuento por múltiples paquetes: 5%");
        }

        // Descuento por promoción activa (se aplica la de mayor porcentaje)
        List<PromotionEntity> activePromotions = promotionRepository
                .findByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        LocalDate.now(), LocalDate.now());
        if (!activePromotions.isEmpty()) {
            PromotionEntity bestPromotion = activePromotions.stream()
                    .max(Comparator.comparingDouble(PromotionEntity::getDiscountPercent))
                    .orElse(null);
            if (bestPromotion != null) {
                discountPercent += bestPromotion.getDiscountPercent();
                int pct = (int) Math.round(bestPromotion.getDiscountPercent() * 100);
                discountDetails.add("Promoción " + bestPromotion.getDescription() + ": " + pct + "%");
            }
        }

        // Cap máximo del 20%
        if (discountPercent >= 0.20) {
            discountPercent = 0.20;
            discountDetails.clear();
            discountDetails.add("Descuento máximo aplicado: 20%");
        }

        double totalDiscount = baseAmount * discountPercent;
        reservation.setBaseAmount(baseAmount);
        reservation.setTotalDiscount(totalDiscount);
        reservation.setFinalAmount(baseAmount - totalDiscount);
        reservation.setDiscountDetail(discountDetails.isEmpty() ? "Sin descuentos" :
                String.join(", ", discountDetails));
        reservation.setStatus("PENDING_PAYMENT");
        reservation.setRegistrationDate(LocalDateTime.now());

        pkg.setAvailableSlots(pkg.getAvailableSlots() - reservation.getPassengerCount());
        if (pkg.getAvailableSlots() == 0) {
            pkg.setStatus("SOLD_OUT");
        }
        tourPackageRepository.save(pkg);

        return reservationRepository.save(reservation);
    }

    public ReservationEntity getReservationById(Long id) {
        return reservationRepository.findById(id).orElse(null);
    }

    public List<ReservationEntity> getReservationsByUser(Long userId) {
        return reservationRepository.findByUserId(userId);
    }

    public List<ReservationEntity> getAllReservations() {
        return reservationRepository.findAll();
    }

    @Transactional
    public ReservationEntity cancelReservation(Long id) {
        ReservationEntity reservation = reservationRepository.findById(id).orElse(null);
        if (reservation == null) return null;
        if ("CANCELLED".equals(reservation.getStatus())) return null;

        // Si estaba pendiente de pago, liberar cupos
        if ("PENDING_PAYMENT".equals(reservation.getStatus())) {
            tourPackageRepository.findById(reservation.getPackageId()).ifPresent(pkg -> {
                pkg.setAvailableSlots(pkg.getAvailableSlots() + reservation.getPassengerCount());
                if (pkg.getAvailableSlots() > 0 && "SOLD_OUT".equals(pkg.getStatus())) {
                    pkg.setStatus("AVAILABLE");
                }
                tourPackageRepository.save(pkg);
            });
        }
        reservation.setStatus("CANCELLED");
        return reservationRepository.save(reservation);
    }
}
