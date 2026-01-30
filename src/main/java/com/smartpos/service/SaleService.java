package com.smartpos.service;

import com.smartpos.model.Sale;
import com.smartpos.repository.SaleRepository;
import com.smartpos.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SaleService {
    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockAlertService stockAlertService;

    @Autowired
    private com.smartpos.util.AppSession session;

    public Sale createSale(Sale sale) {
        if (sale.getTenant() == null) {
            sale.setTenant(session.getCurrentTenant());
        }
        return saleRepository.save(sale);
    }

    public Sale createSale(Sale sale, List<com.smartpos.ui.SalesViewController.CartItem> cartItems) {
        if (sale.getTenant() == null) {
            sale.setTenant(session.getCurrentTenant());
        }

        // Deduct stock
        for (com.smartpos.ui.SalesViewController.CartItem item : cartItems) {
            com.smartpos.model.Product product = item.getProduct();
            // Subtract quantity from stock (both are BigDecimal now)
            product.setStockQuantity(product.getStockQuantity().subtract(item.getQuantity()));
            productRepository.save(product);
        }

        Sale savedSale = saleRepository.save(sale);

        // Trigger stock check for alerts
        stockAlertService.checkStock();

        return savedSale;
    }

    public List<Sale> findAll() {
        return saleRepository.findByTenantId(session.getCurrentTenant().getId());
    }

    public Sale findById(Long id) {
        return saleRepository.findById(id)
                .filter(s -> s.getTenant().getId().equals(session.getCurrentTenant().getId()))
                .orElse(null);
    }
}
