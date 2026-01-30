package com.smartpos.config;

import com.smartpos.model.User;
import com.smartpos.model.Tenant;
import com.smartpos.repository.UserRepository;
import com.smartpos.repository.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Override
    public void run(String... args) throws Exception {
        // 1. Create Default Tenant
        Tenant defaultTenant;
        if (tenantRepository.count() == 0) {
            defaultTenant = new Tenant();
            defaultTenant.setName("Asosiy Do'kon");
            defaultTenant.setOwnerName("Admin");
            defaultTenant.setLicenseKey("FREE-TRIAL-001");
            defaultTenant.setSubscriptionExpiry(LocalDateTime.now().plusYears(1));
            defaultTenant = tenantRepository.save(defaultTenant);
            System.out.println("🏢 Default tenant created: Asosiy Do'kon");
        } else {
            defaultTenant = tenantRepository.findAll().get(0);
        }

        // 2. Create Default Admin
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin"); // In production, use BCrypt
            admin.setPin("1234");
            admin.setFullName("System Administrator");
            admin.setRole(User.Role.ADMIN);
            admin.setActive(true);
            admin.setTenant(defaultTenant); // Associate with tenant
            userRepository.save(admin);
            System.out.println("✅ Default admin user created (admin/admin, PIN: 1234)");
        }
    }
}
