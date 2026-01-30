package com.smartpos.ui;

import com.smartpos.service.SystemConfigService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SettingsViewController {

    @Autowired
    private SystemConfigService configService;

    @FXML
    private TextField storeNameField;
    @FXML
    private TextField storePhoneField;
    @FXML
    private TextArea storeAddressField;
    @FXML
    private TextField receiptFooterField;

    @FXML
    private TextField serverUrlField;
    @FXML
    private Label syncStatusLabel;

    @FXML
    public void initialize() {
        loadSettings();
    }

    private void loadSettings() {
        storeNameField.setText(configService.getConfig("STORE_NAME", "SmartPOS Store"));
        storePhoneField.setText(configService.getConfig("STORE_PHONE", ""));
        storeAddressField.setText(configService.getConfig("STORE_ADDRESS", ""));
        receiptFooterField.setText(configService.getConfig("RECEIPT_FOOTER", "Xaridingiz uchun rahmat!"));
    }

    @FXML
    public void handleSaveSettings() {
        configService.setConfig("STORE_NAME", storeNameField.getText());
        configService.setConfig("STORE_PHONE", storePhoneField.getText());
        configService.setConfig("STORE_ADDRESS", storeAddressField.getText());
        configService.setConfig("RECEIPT_FOOTER", receiptFooterField.getText());

        showAlert("Muvaffaqiyat", "Tizim sozlamalari muvaffaqiyatli saqlandi.");
    }

    @FXML
    public void handleSyncNow() {
        syncStatusLabel.setText("Syncing...");
        // Simulate sync
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                javafx.application.Platform.runLater(() -> {
                    syncStatusLabel.setText("Last synced: Hozir");
                    showAlert("Muvaffaqiyat", "Bulutli tizim bilan ma'lumotlar sinxronizatsiya qilindi.");
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}
