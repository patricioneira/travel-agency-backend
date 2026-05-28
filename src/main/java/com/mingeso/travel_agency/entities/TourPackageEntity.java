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
    private String status;
}
