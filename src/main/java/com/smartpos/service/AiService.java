package com.smartpos.service;

import com.smartpos.model.Product;
import com.smartpos.model.SaleItem;
import com.smartpos.repository.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiService {

    @Autowired
    private SaleRepository saleRepository;

    /**
     * Predicts days of stock remaining for a product based on last 30 days of
     * sales.
     */
    public BigDecimal predictDaysRemaining(Product product) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        List<SaleItem> history = saleRepository.findAll().stream()
                .filter(s -> s.getDate().isAfter(thirtyDaysAgo))
                .flatMap(s -> s.getItems().stream())
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .collect(Collectors.toList());

        BigDecimal totalSold = history.stream()
                .map(SaleItem::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalSold.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.valueOf(999); // Slow mover
        }

        BigDecimal dailyVelocity = totalSold.divide(BigDecimal.valueOf(30), 4, RoundingMode.HALF_UP);
        return product.getStockQuantity().divide(dailyVelocity, 1, RoundingMode.HALF_UP);
    }

    /**
     * Basic "Frequently Bought Together" recommendation.
     */
    /**
     * Real "Frequently Bought Together" recommendation using Co-occurrence
     * analysis.
     * Finds products that appear in the same transaction as the target product.
     */
    public List<Product> getRecommendations(Product product) {
        if (product == null || product.getId() == null) {
            return java.util.Collections.emptyList();
        }

        // 1. Find all sales containing this product
        List<SaleItem> history = saleRepository.findAll().stream()
                .flatMap(s -> s.getItems().stream())
                .collect(Collectors.toList());

        List<Long> saleIdsWithProduct = history.stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .map(item -> item.getSale().getId())
                .distinct()
                .collect(Collectors.toList());

        if (saleIdsWithProduct.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        // 2. Count occurrence of OTHER products in those sales
        java.util.Map<Product, Long> coOccurrenceMap = history.stream()
                .filter(item -> saleIdsWithProduct.contains(item.getSale().getId())) // In same sale
                .filter(item -> !item.getProduct().getId().equals(product.getId())) // Not the same product
                .map(SaleItem::getProduct)
                .collect(Collectors.groupingBy(p -> p, Collectors.counting()));

        // 3. Sort by frequency and return Top 3
        return coOccurrenceMap.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // Descending
                .limit(3)
                .map(java.util.Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
