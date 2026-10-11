package com.project.inventory.service;

import com.project.inventory.model.Product;
import com.project.inventory.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public Product createProduct(Product product) {
        if (productRepository.existsByName(product.getName())) {
            throw new IllegalArgumentException("Product with name '" + product.getName() + "' already exists");
        }
        return productRepository.save(product);
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product getProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
    }

    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> searchProducts(String keyword) {
        return productRepository.searchProducts(keyword);
    }

    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    public Product updateProduct(Long id, Product productDetails) {
        Product product = getProductOrThrow(id);
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setCurrentStock(productDetails.getCurrentStock());
        product.setMinStockThreshold(productDetails.getMinStockThreshold());
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        Product product = getProductOrThrow(id);
        productRepository.delete(product);
    }

    public Product updateStock(Long id, Integer quantityChange) {
        Product product = getProductOrThrow(id);
        int newStock = product.getCurrentStock() + quantityChange;
        if (newStock < 0) {
            throw new IllegalArgumentException("Insufficient stock. Current: " + product.getCurrentStock());
        }
        product.setCurrentStock(newStock);
        return productRepository.save(product);
    }
}