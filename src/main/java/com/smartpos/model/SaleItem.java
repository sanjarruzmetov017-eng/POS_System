package com.smartpos.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Product product;

    private BigDecimal quantity;
    private BigDecimal price; // Price at moment of sale

    @ManyToOne
    @JoinColumn(name = "sale_id")
    private Sale sale;
}
