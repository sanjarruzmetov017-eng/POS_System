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
    public List<Product> getRecommendations(Product product) {
        // Logic: Find sales containing this product, look for other products in those
        // sales.
        return java.util.Collections.emptyList(); // Simulation
    }
}
