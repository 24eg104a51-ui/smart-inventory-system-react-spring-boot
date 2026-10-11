package com.project.inventory.controller;

import com.project.inventory.model.ForecastResult;
import com.project.inventory.service.ForecastService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/forecasts")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ForecastController {

    private final ForecastService forecastService;

    @PostMapping("/product/{productId}")
    public ResponseEntity<ForecastResult> generateForecast(@PathVariable Long productId) {
        return ResponseEntity.ok(forecastService.generateForecast(productId));
    }

    @PostMapping("/product/{productId}/multi-day")
    public ResponseEntity<List<ForecastResult>> generateMultiDayForecast(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(forecastService.generateMultiDayForecast(productId, days));
    }

    @GetMapping("/product/{productId}/latest")
    public ResponseEntity<ForecastResult> getLatestForecast(@PathVariable Long productId) {
        return forecastService.getLatestForecast(productId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/product/{productId}/history")
    public ResponseEntity<List<ForecastResult>> getForecastHistory(@PathVariable Long productId) {
        return ResponseEntity.ok(forecastService.getForecastHistory(productId));
    }

    @GetMapping("/product/{productId}/upcoming")
    public ResponseEntity<List<ForecastResult>> getUpcomingForecasts(
            @PathVariable Long productId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate) {
        return ResponseEntity.ok(forecastService.getUpcomingForecasts(productId, fromDate));
    }

    @DeleteMapping("/product/{productId}")
    public ResponseEntity<Void> deleteForecasts(@PathVariable Long productId) {
        forecastService.deleteForecasts(productId);
        return ResponseEntity.noContent().build();
    }
}