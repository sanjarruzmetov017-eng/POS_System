package com.smartpos.service;

import com.smartpos.model.User;
import com.smartpos.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.smartpos.util.AppSession session;

    public User registerUser(User user) {
        if (user.getTenant() == null && session.getCurrentTenant() != null) {
            user.setTenant(session.getCurrentTenant());
        }
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> loginByPin(String pin) {
        // If we have a tenant context (e.g. from previous selection or common machine),
        // use it
        if (session.getCurrentTenant() != null) {
            return userRepository.findByPinAndTenantId(pin, session.getCurrentTenant().getId());
        }
        return userRepository.findByPin(pin);
    }

    public List<User> findAll() {
        if (session.getCurrentTenant() != null) {
            return userRepository.findByTenantId(session.getCurrentTenant().getId());
        }
        return userRepository.findAll();
    }

    public User save(User user) {
        if (user.getTenant() == null && session.getCurrentTenant() != null) {
            user.setTenant(session.getCurrentTenant());
        }
        return userRepository.save(user);
    }

    public void delete(Long id) {
        findById(id).ifPresent(u -> userRepository.delete(u));
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id)
                .filter(u -> session.getCurrentTenant() == null ||
                        u.getTenant().getId().equals(session.getCurrentTenant().getId()));
    }
}
