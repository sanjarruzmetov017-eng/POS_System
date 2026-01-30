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
public class Expense {
    @ManyToOne(optional = false)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private BigDecimal amount;

    private String category; // e.g., RENT, SALARY, UTILITY, TAX, OTHER

    private LocalDateTime date = LocalDateTime.now();

    @ManyToOne
    private User recordedBy;

    private String note;
}
