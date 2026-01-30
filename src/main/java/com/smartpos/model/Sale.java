package com.smartpos.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sale {
    @ManyToOne(optional = false)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime date = LocalDateTime.now();
    private BigDecimal totalAmount;

    @ManyToOne
    private Customer customer;

    @ManyToOne
    private User user; // The cashier who performed the sale

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "sale")
    private List<SaleItem> items;

    private String paymentMethod; // CASH, CARD, DEBT

    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal paidAmount = BigDecimal.ZERO;
    private BigDecimal balanceDue = BigDecimal.ZERO;

    private boolean synced = false;
}
