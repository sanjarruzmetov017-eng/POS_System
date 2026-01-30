package com.smartpos.service;

import com.smartpos.model.Supplier;
import com.smartpos.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class SupplierService {
    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private com.smartpos.util.AppSession session;

    public List<Supplier> findAll() {
        return supplierRepository.findByTenantId(session.getCurrentTenant().getId());
    }

    public Optional<Supplier> findById(Long id) {
        return supplierRepository.findById(id)
                .filter(s -> s.getTenant().getId().equals(session.getCurrentTenant().getId()));
    }

    public Supplier save(Supplier supplier) {
        if (supplier.getTenant() == null) {
            supplier.setTenant(session.getCurrentTenant());
        }
        return supplierRepository.save(supplier);
    }

    public void delete(Long id) {
        findById(id).ifPresent(s -> supplierRepository.delete(s));
    }
}
