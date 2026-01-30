package com.smartpos.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfig {
    @Id
    private String configKey; // e.g., "STORE_NAME", "STORE_PHONE", "RECEIPT_FOOTER"

    @Column(length = 2000)
    private String configValue;
}
