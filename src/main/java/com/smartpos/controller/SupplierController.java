package com.smartpos.controller;

import com.smartpos.model.Supplier;
import com.smartpos.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {
    @Autowired
    private SupplierService supplierService;

    @GetMapping
    public List<Supplier> getAll() {
        return supplierService.findAll();
    }

    @PostMapping
    public Supplier create(@RequestBody Supplier supplier) {
        return supplierService.save(supplier);
    }
}
