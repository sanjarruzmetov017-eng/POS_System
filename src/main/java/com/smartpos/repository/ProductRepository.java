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

    Optional<Product> findByBarcode(String barcode);

    List<Product> findByStockQuantityLessThan(java.math.BigDecimal threshold);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM Product p WHERE p.tenant.id = :tenantId AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.barcode) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Product> search(@org.springframework.data.repository.query.Param("query") String query,
            @org.springframework.data.repository.query.Param("tenantId") Long tenantId);
}
