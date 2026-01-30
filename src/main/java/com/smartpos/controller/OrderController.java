package com.smartpos.controller;

import com.smartpos.model.Order;
import com.smartpos.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping
    public List<Order> getAll() {
        return orderService.findAll();
    }

    @PostMapping
    public Order create(@RequestBody Order order) {
        return orderService.createOrder(order);
    }
}
