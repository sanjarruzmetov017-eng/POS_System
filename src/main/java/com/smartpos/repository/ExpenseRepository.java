package com.smartpos.repository;

import com.smartpos.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByTenantId(Long tenantId);

    List<Expense> findByTenantIdAndDateBetween(Long tenantId, LocalDateTime start, LocalDateTime end);

    List<Expense> findByTenantIdAndCategory(Long tenantId, String category);
}
