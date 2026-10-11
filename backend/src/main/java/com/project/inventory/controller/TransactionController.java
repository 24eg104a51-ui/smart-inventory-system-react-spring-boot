package com.project.inventory.controller;

import com.project.inventory.model.Transaction;
import com.project.inventory.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<Transaction> recordTransaction(@Valid @RequestBody Transaction transaction) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.recordTransaction(transaction));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<Transaction>> getProductTransactions(
            @PathVariable Long productId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(transactionService.getProductTransactions(productId, pageable));
    }

    @GetMapping("/product/{productId}/all")
    public ResponseEntity<List<Transaction>> getAllProductTransactions(@PathVariable Long productId) {
        return ResponseEntity.ok(transactionService.getProductTransactions(productId));
    }

    @GetMapping("/product/{productId}/period")
    public ResponseEntity<List<Transaction>> getTransactionsInPeriod(
            @PathVariable Long productId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(transactionService.getProductTransactionsInPeriod(productId, start, end));
    }

    @GetMapping("/product/{productId}/total-sales")
    public ResponseEntity<Integer> getTotalSalesInPeriod(
            @PathVariable Long productId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(transactionService.getTotalSalesInPeriod(productId, start, end));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
}