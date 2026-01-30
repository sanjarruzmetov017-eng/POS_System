package com.smartpos.service;

import com.smartpos.model.Expense;
import com.smartpos.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private com.smartpos.util.AppSession session;

    public Expense save(Expense expense) {
        if (expense.getTenant() == null) {
            expense.setTenant(session.getCurrentTenant());
        }
        return expenseRepository.save(expense);
    }

    public List<Expense> findAll() {
        return expenseRepository.findByTenantId(session.getCurrentTenant().getId());
    }

    public List<Expense> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return expenseRepository.findByTenantIdAndDateBetween(session.getCurrentTenant().getId(), start, end);
    }

    public void delete(Long id) {
        expenseRepository.findById(id)
                .filter(e -> e.getTenant().getId().equals(session.getCurrentTenant().getId()))
                .ifPresent(e -> expenseRepository.delete(e));
    }

    public java.math.BigDecimal getTotalExpenses(LocalDateTime start, LocalDateTime end) {
        return findByDateRange(start, end).stream()
                .map(Expense::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }
}
