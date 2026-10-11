package com.project.inventory.service;

import com.project.inventory.model.Product;
import com.project.inventory.model.Transaction;
import com.project.inventory.repository.ProductRepository;
import com.project.inventory.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    public Transaction recordTransaction(Transaction transaction) {
        Product product = productService.getProductOrThrow(transaction.getProduct().getId());
        transaction.setProduct(product);

        if (transaction.getUnitPrice() == null) {
            transaction.setUnitPrice(product.getPrice());
        }
        if (transaction.getTotalAmount() == null) {
            transaction.setTotalAmount(transaction.getUnitPrice().multiply(
                    java.math.BigDecimal.valueOf(transaction.getQuantity())));
        }
        if (transaction.getTransactionDate() == null) {
            transaction.setTransactionDate(LocalDateTime.now());
        }

        Transaction saved = transactionRepository.save(transaction);

        int stockChange = switch (transaction.getTransactionType()) {
            case PURCHASE, RETURN -> transaction.getQuantity();
            case SALE -> -transaction.getQuantity();
            case ADJUSTMENT -> transaction.getQuantity();
        };
        productService.updateStock(product.getId(), stockChange);

        return saved;
    }

    public List<Transaction> getProductTransactions(Long productId) {
        return transactionRepository.findByProductIdOrderByTransactionDateDesc(productId);
    }

    public Page<Transaction> getProductTransactions(Long productId, Pageable pageable) {
        return transactionRepository.findByProductIdOrderByTransactionDateDesc(productId, pageable);
    }

    public List<Transaction> getProductTransactionsInPeriod(Long productId, LocalDateTime start, LocalDateTime end) {
        return transactionRepository.findByProductIdAndDateRange(productId, start, end);
    }

    public Integer getTotalSalesInPeriod(Long productId, LocalDateTime start, LocalDateTime end) {
        Integer total = transactionRepository.getTotalSalesInPeriod(productId, start, end);
        return total != null ? total : 0;
    }

    public void deleteTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found with id: " + id));
        
        int stockChange = switch (transaction.getTransactionType()) {
            case PURCHASE, RETURN -> -transaction.getQuantity();
            case SALE -> transaction.getQuantity();
            case ADJUSTMENT -> -transaction.getQuantity();
        };
        productService.updateStock(transaction.getProduct().getId(), stockChange);
        
        transactionRepository.delete(transaction);
    }
}