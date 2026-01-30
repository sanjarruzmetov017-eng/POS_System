package com.smartpos.repository;

import com.smartpos.model.ProductBatch;
import com.smartpos.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductBatchRepository extends JpaRepository<ProductBatch, Long> {
    List<ProductBatch> findByProductAndActiveOrderByExpiryDateAsc(Product product, boolean active);

    List<ProductBatch> findByProductAndActiveOrderByArrivalDateAsc(Product product, boolean active);
}
