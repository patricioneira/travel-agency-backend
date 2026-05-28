package com.mingeso.travel_agency.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackageRankingDTO {
    private Long packageId;
    private Long reservationCount;
    private Long totalPassengers;
    private Double totalAmount;
}
