package com.project.inventory.service;

import com.project.inventory.ml.DemandForecaster;
import com.project.inventory.model.ForecastResult;
import com.project.inventory.model.Product;
import com.project.inventory.model.Transaction;
import com.project.inventory.repository.ForecastResultRepository;
import com.project.inventory.repository.ProductRepository;
import com.project.inventory.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ForecastService {

    private final ForecastResultRepository forecastRepository;
    private final ProductRepository productRepository;
    private final TransactionRepository transactionRepository;

    public ForecastResult generateForecast(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + productId));

        List<Transaction> transactions = transactionRepository.findByProductIdOrderByTransactionDateDesc(productId);
        ForecastResult forecast = DemandForecaster.forecastDemand(product, transactions);
        
        forecastRepository.deleteByProductId(productId);
        return forecastRepository.save(forecast);
    }

    public List<ForecastResult> generateMultiDayForecast(Long productId, int days) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + productId));

        List<Transaction> transactions = transactionRepository.findByProductIdOrderByTransactionDateDesc(productId);
        List<ForecastResult> forecasts = DemandForecaster.generateMultiDayForecast(product, transactions, days);
        
        forecastRepository.deleteByProductId(productId);
        return forecastRepository.saveAll(forecasts);
    }

    public Optional<ForecastResult> getLatestForecast(Long productId) {
        List<ForecastResult> forecasts = forecastRepository.findByProductIdOrderByForecastDateDesc(productId);
        return forecasts.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(forecasts.get(0));
    }

    public List<ForecastResult> getForecastHistory(Long productId) {
        return forecastRepository.findByProductIdOrderByForecastDateDesc(productId);
    }

    public List<ForecastResult> getUpcomingForecasts(Long productId, LocalDate fromDate) {
        return forecastRepository.findByProductIdAndDateRange(productId, fromDate);
    }

    public void deleteForecasts(Long productId) {
        forecastRepository.deleteByProductId(productId);
    }
}