package com.smartpos.repository;

import com.smartpos.model.PriceHistory;
import com.smartpos.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
    List<PriceHistory> findByProductOrderByChangeDateDesc(Product product);
}
