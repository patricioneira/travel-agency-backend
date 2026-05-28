package com.mingeso.travel_agency.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long packageId;
    private Integer passengerCount;

    private Double baseAmount;
    private Double totalDiscount;
    private Double finalAmount;

    // Épica 4: detalle de descuentos aplicados para mostrar al usuario
    // Ejemplo: "Descuento por grupo: 10%, Cliente frecuente: 10%"
    @Column(length = 500)
    private String discountDetail;

    private LocalDateTime registrationDate = LocalDateTime.now();

    // Estados: PENDING_PAYMENT, CONFIRMED, CANCELLED
    private String status;
}
