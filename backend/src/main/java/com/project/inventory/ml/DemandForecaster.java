package com.project.inventory.ml;

import com.project.inventory.model.ForecastResult;
import com.project.inventory.model.Product;
import com.project.inventory.model.Transaction;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LinearRegression;
import weka.core.*;
import weka.core.converters.ConverterUtils.DataSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class DemandForecaster {

    private static final int DAYS_OF_HISTORY = 90;
    private static final int FORECAST_HORIZON = 30;
    private static final int MIN_DATA_POINTS = 5;

    public static ForecastResult forecastDemand(Product product, List<Transaction> transactions) {
        try {
            Instances dataset = createDataset(product, transactions);
            if (dataset.numInstances() < MIN_DATA_POINTS) {
                return createSmartFallbackForecast(product, transactions);
            }

            Classifier model = new LinearRegression();
            model.buildClassifier(dataset);

            Evaluation evaluation = new Evaluation(dataset);
            evaluation.crossValidateModel(model, dataset, 5, new java.util.Random(1));

            return generateForecast(product, transactions, model, dataset, evaluation);
        } catch (Exception e) {
            System.err.println("Forecasting error for product " + product.getId() + ": " + e.getMessage());
            return createSmartFallbackForecast(product, transactions);
        }
    }

    private static Instances createDataset(Product product, List<Transaction> transactions) {
        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("dayOfWeek"));
        attributes.add(new Attribute("dayOfMonth"));
        attributes.add(new Attribute("month"));
        attributes.add(new Attribute("lag1"));
        attributes.add(new Attribute("lag7"));
        attributes.add(new Attribute("lag30"));
        attributes.add(new Attribute("demand"));

        Instances dataset = new Instances("DemandData", attributes, 0);
        dataset.setClassIndex(dataset.numAttributes() - 1);

        Map<LocalDate, Integer> dailySales = aggregateDailySales(transactions);
        List<LocalDate> sortedDates = new ArrayList<>(dailySales.keySet());
        sortedDates.sort(LocalDate::compareTo);

        for (int i = 30; i < sortedDates.size(); i++) {
            LocalDate currentDate = sortedDates.get(i);
            int demand = dailySales.getOrDefault(currentDate, 0);

            double dayOfWeek = currentDate.getDayOfWeek().getValue();
            double dayOfMonth = currentDate.getDayOfMonth();
            double month = currentDate.getMonthValue();

            double lag1 = dailySales.getOrDefault(currentDate.minusDays(1), 0);
            double lag7 = dailySales.getOrDefault(currentDate.minusDays(7), 0);
            double lag30 = dailySales.getOrDefault(currentDate.minusDays(30), 0);

            double[] values = {dayOfWeek, dayOfMonth, month, lag1, lag7, lag30, demand};
            dataset.add(new DenseInstance(1.0, values));
        }

        return dataset;
    }

    private static Map<LocalDate, Integer> aggregateDailySales(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getTransactionType() == Transaction.TransactionType.SALE)
                .collect(Collectors.groupingBy(
                        t -> t.getTransactionDate().toLocalDate(),
                        Collectors.summingInt(Transaction::getQuantity)
                ));
    }

    private static ForecastResult generateForecast(Product product, List<Transaction> transactions, Classifier model, Instances dataset, Evaluation evaluation) {
        ForecastResult result = new ForecastResult();
        result.setProduct(product);
        result.setModelUsed("LinearRegression");
        result.setAccuracyScore(BigDecimal.valueOf(Math.max(0, 1 - evaluation.rootMeanSquaredError() / 100)));

        Map<LocalDate, Integer> dailySales = aggregateDailySales(transactions);

        LocalDate lastDate = dailySales.keySet().stream().max(LocalDate::compareTo).orElse(LocalDate.now());
        int lag1 = dailySales.getOrDefault(lastDate, 0);
        int lag7 = dailySales.getOrDefault(lastDate.minusDays(7), 0);
        int lag30 = dailySales.getOrDefault(lastDate.minusDays(30), 0);

        LocalDate forecastDate = lastDate.plusDays(1);
        double dayOfWeek = forecastDate.getDayOfWeek().getValue();
        double dayOfMonth = forecastDate.getDayOfMonth();
        double month = forecastDate.getMonthValue();

        try {
            double[] values = {dayOfWeek, dayOfMonth, month, lag1, lag7, lag30, 0};
            Instance instance = new DenseInstance(1.0, values);
            instance.setDataset(dataset);
            double prediction = model.classifyInstance(instance);
            int predictedDemand = Math.max(0, (int) Math.round(prediction));

            result.setForecastDate(forecastDate);
            result.setPredictedDemand(predictedDemand);
            result.setConfidenceLower(Math.max(0, predictedDemand - (int)Math.ceil(predictedDemand * 0.2)));
            result.setConfidenceUpper(predictedDemand + (int)Math.ceil(predictedDemand * 0.2));
        } catch (Exception e) {
            return createSmartFallbackForecast(product, transactions);
        }

        return result;
    }

    private static ForecastResult createSmartFallbackForecast(Product product, List<Transaction> transactions) {
        Map<LocalDate, Integer> dailySales = aggregateDailySales(transactions);
        
        ForecastResult result = new ForecastResult();
        result.setProduct(product);
        result.setForecastDate(LocalDate.now().plusDays(1));
        result.setModelUsed("Smart-Fallback");

        if (dailySales.isEmpty()) {
            result.setPredictedDemand(5);
            result.setConfidenceLower(2);
            result.setConfidenceUpper(8);
            result.setAccuracyScore(BigDecimal.valueOf(0.3));
            return result;
        }

        List<Integer> salesValues = new ArrayList<>(dailySales.values());
        double avgDaily = salesValues.stream().mapToInt(Integer::intValue).average().orElse(5);
        
        LocalDate lastDate = dailySales.keySet().stream().max(LocalDate::compareTo).orElse(LocalDate.now());
        int recentSales = dailySales.getOrDefault(lastDate, 0);
        int weekAgoSales = dailySales.getOrDefault(lastDate.minusDays(7), 0);
        
        double trend = 1.0;
        if (weekAgoSales > 0) {
            trend = (double) recentSales / weekAgoSales;
            trend = Math.max(0.5, Math.min(2.0, trend));
        }

        int predictedDemand = (int) Math.round(avgDaily * trend);
        predictedDemand = Math.max(1, predictedDemand);
        
        int margin = Math.max(2, (int) Math.ceil(predictedDemand * 0.3));
        
        result.setPredictedDemand(predictedDemand);
        result.setConfidenceLower(Math.max(0, predictedDemand - margin));
        result.setConfidenceUpper(predictedDemand + margin);
        result.setAccuracyScore(BigDecimal.valueOf(0.55).setScale(2, RoundingMode.HALF_UP));
        
        return result;
    }

    public static List<ForecastResult> generateMultiDayForecast(Product product, List<Transaction> transactions, int days) {
        List<ForecastResult> forecasts = new ArrayList<>();
        Map<LocalDate, Integer> dailySales = aggregateDailySales(transactions);
        
        try {
            Instances dataset = createDataset(product, transactions);
            if (dataset.numInstances() >= MIN_DATA_POINTS) {
                Classifier model = new LinearRegression();
                model.buildClassifier(dataset);

                LocalDate lastDate = dailySales.keySet().stream().max(LocalDate::compareTo).orElse(LocalDate.now());
                
                for (int d = 1; d <= days; d++) {
                    LocalDate forecastDate = lastDate.plusDays(d);
                    int lag1 = dailySales.getOrDefault(forecastDate.minusDays(1), 0);
                    int lag7 = dailySales.getOrDefault(forecastDate.minusDays(7), 0);
                    int lag30 = dailySales.getOrDefault(forecastDate.minusDays(30), 0);

                    double[] values = {
                        forecastDate.getDayOfWeek().getValue(),
                        forecastDate.getDayOfMonth(),
                        forecastDate.getMonthValue(),
                        lag1, lag7, lag30, 0
                    };
                    
                    Instance instance = new DenseInstance(1.0, values);
                    instance.setDataset(dataset);
                    double prediction = model.classifyInstance(instance);
                    int predictedDemand = Math.max(0, (int) Math.round(prediction));

                    ForecastResult result = new ForecastResult();
                    result.setProduct(product);
                    result.setForecastDate(forecastDate);
                    result.setPredictedDemand(predictedDemand);
                    result.setConfidenceLower(Math.max(0, predictedDemand - (int)Math.ceil(predictedDemand * 0.2)));
                    result.setConfidenceUpper(predictedDemand + (int)Math.ceil(predictedDemand * 0.2));
                    result.setModelUsed("LinearRegression");
                    forecasts.add(result);
                }
            }
        } catch (Exception e) {
            System.err.println("Multi-day forecast error: " + e.getMessage());
        }

        if (forecasts.isEmpty()) {
            List<Integer> salesValues = new ArrayList<>(dailySales.values());
            double avgDaily = salesValues.isEmpty() ? 5 : salesValues.stream().mapToInt(Integer::intValue).average().orElse(5);
            
            LocalDate lastDate = dailySales.keySet().stream().max(LocalDate::compareTo).orElse(LocalDate.now());
            int recentSales = dailySales.getOrDefault(lastDate, 0);
            int weekAgoSales = dailySales.getOrDefault(lastDate.minusDays(7), 0);
            double trend = weekAgoSales > 0 ? Math.max(0.5, Math.min(2.0, (double) recentSales / weekAgoSales)) : 1.0;

            for (int d = 1; d <= days; d++) {
                LocalDate forecastDate = LocalDate.now().plusDays(d);
                double dayOfWeekFactor = getDayOfWeekFactor(forecastDate.getDayOfWeek().getValue());
                
                int baseDemand = (int) Math.round(avgDaily * trend * dayOfWeekFactor);
                baseDemand = Math.max(1, baseDemand);
                
                int margin = Math.max(2, (int) Math.ceil(baseDemand * 0.3));

                ForecastResult result = new ForecastResult();
                result.setProduct(product);
                result.setForecastDate(forecastDate);
                result.setPredictedDemand(baseDemand);
                result.setConfidenceLower(Math.max(0, baseDemand - margin));
                result.setConfidenceUpper(baseDemand + margin);
                result.setModelUsed("Smart-Fallback");
                result.setAccuracyScore(BigDecimal.valueOf(0.55).setScale(2, RoundingMode.HALF_UP));
                forecasts.add(result);
            }
        }

        return forecasts;
    }

    private static double getDayOfWeekFactor(int dayOfWeek) {
        return switch (dayOfWeek) {
            case 6, 7 -> 1.3; // Saturday, Sunday
            case 5 -> 1.1;    // Friday
            case 1 -> 0.9;    // Monday
            default -> 1.0;   // Tue-Thu
        };
    }
}