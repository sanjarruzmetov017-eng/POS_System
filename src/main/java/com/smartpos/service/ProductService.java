package com.smartpos.service;

import com.smartpos.model.Product;
import com.smartpos.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private com.smartpos.repository.PriceHistoryRepository priceHistoryRepository;

    @Autowired
    private com.smartpos.service.AuditService auditService;

    @Autowired
    private com.smartpos.service.UserService userService;

    @Autowired
    private com.smartpos.util.AppSession session;

    public List<Product> findAll() {
        return productRepository.findByTenantId(session.getCurrentTenant().getId());
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id)
                .filter(p -> p.getTenant().getId().equals(session.getCurrentTenant().getId()));
    }

    public Product save(Product product) {
        if (product.getTenant() == null) {
            product.setTenant(session.getCurrentTenant());
        }

        if (product.getId() != null) {
            Optional<Product> existing = productRepository.findById(product.getId());
            if (existing.isPresent()) {
                Product old = existing.get();
                if (old.getPrice().compareTo(product.getPrice()) != 0) {
                    // Log price change
                    com.smartpos.model.User currentUser = session.getCurrentUser();

                    com.smartpos.model.PriceHistory history = new com.smartpos.model.PriceHistory();
                    history.setProduct(product);
                    history.setOldPrice(old.getPrice());
                    history.setNewPrice(product.getPrice());
                    history.setChangedBy(currentUser);
                    priceHistoryRepository.save(history);

                    auditService.log(currentUser, "PRICE_CHANGE",
                            "Price updated from " + old.getPrice() + " to " + product.getPrice() + " for "
                                    + product.getName(),
                            "Product", product.getId());
                }
            }
        }
        return productRepository.save(product);
    }

    public void delete(Long id) {
        findById(id).ifPresent(p -> productRepository.delete(p));
    }

    public List<Product> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return findAll();
        }
        return productRepository.search(query.trim(), session.getCurrentTenant().getId());
    }

    public List<Product> findByCategory(Long catId) {
        // Need to add findByCategoryIdAndTenantId
        return productRepository.findByCategoryId(catId).stream()
                .filter(p -> p.getTenant().getId().equals(session.getCurrentTenant().getId()))
                .toList();
    }

    public List<Product> getLowStock(java.math.BigDecimal threshold) {
        return productRepository.findByStockQuantityLessThan(threshold).stream()
                .filter(p -> p.getTenant().getId().equals(session.getCurrentTenant().getId()))
                .toList();
    }

    public Optional<Product> findByBarcode(String barcode) {
        return productRepository.findByBarcode(barcode)
                .filter(p -> p.getTenant().getId().equals(session.getCurrentTenant().getId()));
    }
}
