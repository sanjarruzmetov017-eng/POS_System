package com.smartpos.repository;

import com.smartpos.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findByTenantId(Long tenantId);

    List<Sale> findBySyncedFalse();

    List<Sale> findBySyncedFalseAndTenantId(Long tenantId);
}
