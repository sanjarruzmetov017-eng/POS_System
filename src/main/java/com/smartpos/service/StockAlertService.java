package com.smartpos.service;

import com.smartpos.model.Product;
import com.smartpos.repository.ProductRepository;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockAlertService {

    @Autowired
    private ProductRepository productRepository;

    private final IntegerProperty lowStockCount = new SimpleIntegerProperty(0);

    public IntegerProperty lowStockCountProperty() {
        return lowStockCount;
    }

    public int getLowStockCount() {
        return lowStockCount.get();
    }

    @Scheduled(fixedRate = 60000) // Check every 1 minute
    public void updateLowStockCount() {
        checkStock();
    }

    public void checkStock() {
        List<Product> allProducts = productRepository.findAll();
        int count = 0;
        for (Product product : allProducts) {
            if (product.getStockQuantity() != null && product.getLowStockThreshold() != null &&
                    product.getStockQuantity().compareTo(product.getLowStockThreshold()) <= 0) {
                count++;
            }
        }

        final int finalCount = count;
        Platform.runLater(() -> lowStockCount.set(finalCount));
    }
}
