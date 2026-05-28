package com.mingeso.travel_agency.controllers;

import com.mingeso.travel_agency.dto.PackageRankingDTO;
import com.mingeso.travel_agency.entities.ReservationEntity;
import com.mingeso.travel_agency.services.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ReportController {
    private final ReportService reportService;

    // Reporte 1: listado de ventas por período
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/sales")
    public ResponseEntity<List<ReservationEntity>> getSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        if (start.isAfter(end)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(reportService.getSalesReport(start, end));
    }

    // Reporte 2: ranking de paquetes con conteo, pasajeros y monto total
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/ranking")
    public ResponseEntity<List<PackageRankingDTO>> getPackageRanking(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        if (start.isAfter(end)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(reportService.getPackageRanking(start, end));
    }
}
