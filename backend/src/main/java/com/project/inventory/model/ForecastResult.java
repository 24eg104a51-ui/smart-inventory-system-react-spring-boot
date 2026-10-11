package com.project.inventory.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "forecast_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForecastResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull
    @Column(name = "forecast_date", nullable = false)
    private LocalDate forecastDate;

    @NotNull
    @Column(name = "predicted_demand", nullable = false)
    private Integer predictedDemand;

    @Column(name = "confidence_lower")
    private Integer confidenceLower;

    @Column(name = "confidence_upper")
    private Integer confidenceUpper;

    @Column(name = "model_used", length = 50)
    private String modelUsed;

    @Column(name = "accuracy_score", precision = 5, scale = 4)
    private BigDecimal accuracyScore;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}