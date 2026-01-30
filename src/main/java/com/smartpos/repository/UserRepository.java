package com.smartpos.repository;

import com.smartpos.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameAndTenantId(String username, Long tenantId);

    Optional<User> findByUsername(String username); // Global admin or first login

    Optional<User> findByPinAndTenantId(String pin, Long tenantId);

    Optional<User> findByPin(String pin);

    List<User> findByTenantId(Long tenantId);
}
