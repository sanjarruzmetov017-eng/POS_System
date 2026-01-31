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
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = { "barcode", "tenant_id" })
})
public class Product {
    @ManyToOne(optional = false)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String barcode; // Uniqueness handled by @Table constraint now

    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private BigDecimal costPrice;

    private BigDecimal stockQuantity;
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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    private java.util.List<String> imageUrls = new java.util.ArrayList<>();

    public enum UnitType {
        PIECE, KG, GRAM, LITER, PACK
    }
}
