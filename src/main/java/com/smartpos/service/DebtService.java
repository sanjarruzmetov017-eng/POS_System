package com.smartpos.service;

import com.smartpos.model.Customer;
import com.smartpos.model.Sale;
import com.smartpos.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class DebtService {

    @Autowired
    private CustomerRepository customerRepository;

    public void recordDebt(Sale sale) {
        if (sale.getCustomer() == null || sale.getBalanceDue().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        Customer customer = sale.getCustomer();
        customer.setDebtBalance(customer.getDebtBalance().add(sale.getBalanceDue()));
        customerRepository.save(customer);
    }
}
