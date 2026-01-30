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
public class Product {
    @ManyToOne(optional = false)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String barcode;

    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private BigDecimal costPrice;

    private BigDecimal stockQuantity; // Changed from int to BigDecimal for KG support
    private BigDecimal lowStockThreshold;

    @Enumerated(EnumType.STRING)
    private UnitType unitType = UnitType.PIECE;

    private boolean weighted = false;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    private String imageUrl;

    public enum UnitType {
        PIECE, KG, GRAM, LITER, PACK
    }
}
