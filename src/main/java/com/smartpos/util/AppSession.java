package com.smartpos.util;

import com.smartpos.model.User;
import com.smartpos.model.Tenant;
import org.springframework.stereotype.Component;

@Component
public class AppSession {
    private User currentUser;
    private Tenant currentTenant;

    public void login(User user) {
        this.currentUser = user;
        this.currentTenant = user.getTenant();
    }

    public void logout() {
        this.currentUser = null;
        this.currentTenant = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Tenant getCurrentTenant() {
        return currentTenant;
    }

    public void setCurrentTenant(Tenant tenant) {
        this.currentTenant = tenant;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == User.Role.ADMIN;
    }
}
