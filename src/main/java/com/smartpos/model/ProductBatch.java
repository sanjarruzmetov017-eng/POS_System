package com.smartpos.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductBatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private String batchNumber;

    @Column(nullable = false)
    private BigDecimal quantity;

    private LocalDate arrivalDate = LocalDate.now();

    private LocalDate expiryDate;

    private BigDecimal buyPrice; // Price can vary per batch

    private boolean active = true;
}
