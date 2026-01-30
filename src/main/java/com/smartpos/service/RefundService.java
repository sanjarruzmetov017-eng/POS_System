package com.smartpos.service;

import com.smartpos.model.Sale;
import com.smartpos.model.SaleItem;
import com.smartpos.model.Product;
import com.smartpos.repository.SaleRepository;
import com.smartpos.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefundService {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockAlertService stockAlertService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private UserService userService;

    @Transactional
    public void refundSale(Long saleId, String reason) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new IllegalArgumentException("Sale not found"));

        if (sale.getPaymentMethod().equals("REFUNDED")) {
            throw new IllegalStateException("Sale already refunded");
        }

        // Restore stock
        for (SaleItem item : sale.getItems()) {
            Product product = item.getProduct();
            // Add quantity back to stock (both are BigDecimal now)
            product.setStockQuantity(product.getStockQuantity().add(item.getQuantity()));
            productRepository.save(product);
        }

        // Update sale status
        sale.setPaymentMethod("REFUNDED");
        saleRepository.save(sale);

        // Audit refund
        com.smartpos.model.User currentUser = userService.findAll().get(0); // Placeholder
        auditService.log(currentUser, "REFUND",
                "Refunded sale #" + saleId + " for amount " + sale.getTotalAmount(),
                "Sale", saleId);

        // Check stock levels again (as they might have gone above threshold)
        stockAlertService.checkStock();
    }
}
