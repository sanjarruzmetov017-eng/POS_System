package com.smartpos.repository;

import com.smartpos.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    Optional<Tenant> findByLicenseKey(String licenseKey);

    Optional<Tenant> findByName(String name);
}
