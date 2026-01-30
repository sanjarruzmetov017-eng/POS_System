package com.smartpos.service;

import com.smartpos.model.Customer;
import com.smartpos.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private com.smartpos.util.AppSession session;

    public Customer save(Customer customer) {
        if (customer.getTenant() == null) {
            customer.setTenant(session.getCurrentTenant());
        }
        return customerRepository.save(customer);
    }

    public List<Customer> findAll() {
        return customerRepository.findByTenantId(session.getCurrentTenant().getId());
    }

    public Customer findById(Long id) {
        return customerRepository.findById(id)
                .filter(c -> c.getTenant().getId().equals(session.getCurrentTenant().getId()))
                .orElse(null);
    }
}
