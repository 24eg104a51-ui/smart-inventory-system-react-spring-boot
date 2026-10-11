package com.project.inventory.repository;

import com.project.inventory.model.ForecastResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ForecastResultRepository extends JpaRepository<ForecastResult, Long> {
    Optional<ForecastResult> findByProductIdAndForecastDate(Long productId, LocalDate forecastDate);

    @Query("SELECT f FROM ForecastResult f WHERE f.product.id = :productId AND f.forecastDate >= :startDate ORDER BY f.forecastDate ASC")
    List<ForecastResult> findByProductIdAndDateRange(
            @Param("productId") Long productId,
            @Param("startDate") LocalDate startDate);

    List<ForecastResult> findByProductIdOrderByForecastDateDesc(Long productId);

    @Query("DELETE FROM ForecastResult f WHERE f.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);
}