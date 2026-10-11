package com.project.inventory.repository;

import com.project.inventory.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByProductIdOrderByTransactionDateDesc(Long productId);

    Page<Transaction> findByProductIdOrderByTransactionDateDesc(Long productId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.product.id = :productId AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<Transaction> findByProductIdAndDateRange(
            @Param("productId") Long productId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(t.quantity) FROM Transaction t WHERE t.product.id = :productId AND t.transactionType = 'SALE' AND t.transactionDate BETWEEN :startDate AND :endDate")
    Integer getTotalSalesInPeriod(
            @Param("productId") Long productId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}