package com.smartpos.service;

import com.smartpos.model.Customer;
import com.smartpos.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class LoyaltyService {

    @Autowired
    private CustomerRepository customerRepository;

    public void awardPoints(Customer customer, BigDecimal amount) {
        if (customer == null)
            return;

        // 1 point for every 10 units spent
        int points = amount.divideToIntegralValue(new BigDecimal("10")).intValue();
        customer.setLoyaltyPoints(customer.getLoyaltyPoints() + points);
        customerRepository.save(customer);
    }
}
