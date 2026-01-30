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
public class CashSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime startTime = LocalDateTime.now();
    private LocalDateTime endTime;

    private BigDecimal startCash = BigDecimal.ZERO;
    private BigDecimal endCash;
    private BigDecimal expectedEndCash;
    private BigDecimal discrepancy;

    private boolean open = true;
    private String closingNotes;
}
