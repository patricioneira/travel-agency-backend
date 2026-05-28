package com.mingeso.travel_agency.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "promotions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    // Porcentaje como decimal: 0.05 = 5%, 0.10 = 10%
    private Double discountPercent;

    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active = true;
}
