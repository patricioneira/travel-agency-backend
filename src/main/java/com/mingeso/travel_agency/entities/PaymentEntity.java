package com.mingeso.travel_agency.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Long reservationId;

    private LocalDateTime paymentDate = LocalDateTime.now();
    private Double amount;

    // Solo se acepta tarjeta de crédito simulada
    private String paymentMethod;

    // Épica 5: datos de tarjeta simulada
    private String cardNumber;       // número de tarjeta (simulado)
    private String expirationDate;   // fecha de expiración (simulado, ej: "12/27")
    private String cvv;              // código de seguridad (simulado)

    // Estado: APPROVED
    private String status;
}
