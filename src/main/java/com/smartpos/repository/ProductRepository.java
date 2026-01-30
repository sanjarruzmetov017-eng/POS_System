package com.smartpos.repository;

import com.smartpos.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByTenantId(Long tenantId);

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findBySupplierId(Long supplierId);

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByStockQuantityLessThan(java.math.BigDecimal threshold);

    Optional<Product> findByBarcode(String barcode);
}
