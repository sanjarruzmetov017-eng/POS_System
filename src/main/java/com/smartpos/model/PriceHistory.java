package com.smartpos.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private BigDecimal oldPrice;
    private BigDecimal newPrice;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User changedBy;

    private LocalDateTime changeDate = LocalDateTime.now();
}
