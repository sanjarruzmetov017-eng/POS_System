package com.smartpos.ui;

import com.smartpos.model.Tenant;
import com.smartpos.model.User;
import com.smartpos.repository.TenantRepository;
import com.smartpos.repository.UserRepository;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class TenantDialogController {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @FXML
    private TextField nameField;
    @FXML
    private TextField ownerField;
    @FXML
    private TextField adminUserField;
    @FXML
    private PasswordField adminPassField;
    @FXML
    private DatePicker expiryPicker;

    @FXML
    public void initialize() {
        expiryPicker.setValue(LocalDate.now().plusYears(1));
    }

    @FXML
    private void handleSave() {
        if (nameField.getText().isEmpty() || adminUserField.getText().isEmpty()) {
            return;
        }

        // 1. Create Tenant
        Tenant tenant = new Tenant();
        tenant.setName(nameField.getText());
        tenant.setOwnerName(ownerField.getText());
        tenant.setLicenseKey("SP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        tenant.setSubscriptionExpiry(expiryPicker.getValue().atStartOfDay());
        tenant = tenantRepository.save(tenant);

        // 2. Create Initial Admin for this Tenant
        User admin = new User();
        admin.setUsername(adminUserField.getText());
        admin.setPassword(adminPassField.getText());
        admin.setFullName(ownerField.getText());
        admin.setRole(User.Role.ADMIN);
        admin.setActive(true);
        admin.setTenant(tenant);
        admin.setPin("1234"); // Default PIN
        userRepository.save(admin);

        closeStage();
    }

    @FXML
    private void handleCancel() {
        closeStage();
    }

    private void closeStage() {
        ((Stage) nameField.getScene().getWindow()).close();
    }
}
