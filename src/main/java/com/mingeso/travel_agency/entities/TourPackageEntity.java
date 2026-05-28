package com.mingeso.travel_agency.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "tour_packages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TourPackageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String destination;

    @Column(length = 1000)
    private String description;

    private LocalDate startDate;
    private LocalDate endDate;
    private Double price;
    private Integer totalSlots;
    private Integer availableSlots;

    // Épica 2: atributos adicionales del enunciado
    @Column(length = 1000)
    private String services;       // servicios incluidos (hotel, vuelo, traslado, etc.)

    @Column(length = 1000)
    private String conditions;     // condiciones del paquete

    @Column(length = 1000)
    private String restrictions;   // restricciones

    private String travelType;     // tipo de viaje: NATIONAL / INTERNATIONAL
    private String season;         // temporada: HIGH / LOW / MID
    private String category;       // categoría: ADVENTURE / BEACH / CULTURAL / etc.

    // Estados válidos: AVAILABLE, SOLD_OUT, NOT_VALID, CANCELLED
    private String status;
}
