package com.smartpos.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // Do'kon nomi

    private String ownerName; // Do'kon egasi
    private String phone;
    private String address;

    @Column(nullable = false, unique = true)
    private String licenseKey; // Litsenziya kaliti

    private boolean active = true;

    private LocalDateTime subscriptionExpiry; // Obuna muddati
    private LocalDateTime createdAt = LocalDateTime.now();
}
