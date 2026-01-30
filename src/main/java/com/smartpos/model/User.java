package com.smartpos.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @ManyToOne
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password; // Hashed

    private String fullName;

    @Column(length = 6)
    private String pin; // Numeric PIN for faster access

    // Simple Role Management for now: ADMIN, CASHIER, MANAGER
    @Enumerated(EnumType.STRING)
    private Role role;

    private boolean active = true;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Role {
        ADMIN, MANAGER, CASHIER
    }
}
