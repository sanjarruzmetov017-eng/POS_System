package com.smartpos.controller;

import com.smartpos.model.Sale;
import com.smartpos.service.SaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sales")
public class SaleController {
    @Autowired
    private SaleService saleService;

    @PostMapping
    public Sale create(@RequestBody Sale sale) {
        return saleService.createSale(sale);
    }

    @GetMapping
    public List<Sale> getAll() {
        return saleService.findAll();
    }
}
