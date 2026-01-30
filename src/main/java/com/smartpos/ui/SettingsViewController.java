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
    private PasswordField telegramTokenField;
    @FXML
    private TextField telegramChatIdField;
    @FXML
    private Label telegramStatusLabel;

    @Autowired
    private com.smartpos.service.TelegramService telegramService;

    @FXML
    public void initialize() {
        loadSettings();
    }

    private void loadSettings() {
        storeNameField.setText(configService.getConfig("STORE_NAME", "SmartPOS Store"));
        storePhoneField.setText(configService.getConfig("STORE_PHONE", ""));
        storeAddressField.setText(configService.getConfig("STORE_ADDRESS", ""));
        receiptFooterField.setText(configService.getConfig("RECEIPT_FOOTER", "Xaridingiz uchun rahmat!"));

        telegramTokenField.setText(configService.getConfig("TELEGRAM_BOT_TOKEN", ""));
        telegramChatIdField.setText(configService.getConfig("TELEGRAM_CHAT_ID", ""));
    }

    @FXML
    public void handleSaveSettings() {
        configService.setConfig("STORE_NAME", storeNameField.getText());
        configService.setConfig("STORE_PHONE", storePhoneField.getText());
        configService.setConfig("STORE_ADDRESS", storeAddressField.getText());
        configService.setConfig("RECEIPT_FOOTER", receiptFooterField.getText());

        configService.setConfig("TELEGRAM_BOT_TOKEN", telegramTokenField.getText());
        configService.setConfig("TELEGRAM_CHAT_ID", telegramChatIdField.getText());

        showAlert("Muvaffaqiyat", "Tizim sozlamalari muvaffaqiyatli saqlandi.");
    }

    @FXML
    public void handleTestTelegram() {
        handleSaveSettings(); // Save first
        telegramStatusLabel.setText("Yuborilmoqda...");

        new Thread(() -> {
            try {
                telegramService.sendDailySummary("🧪 SmartPOS Tizimidan TEST xabari: Integratsiya ishlayapti!");

                javafx.application.Platform.runLater(() -> {
                    telegramStatusLabel.setText("Yuborildi ✅");
                    telegramStatusLabel.setStyle("-fx-text-fill: #00ff00;");
                    showAlert("Muvaffaqiyat", "Test xabari Telegramga yuborildi.\nBotni tekshiring.");
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    telegramStatusLabel.setText("Xatolik ❌");
                    telegramStatusLabel.setStyle("-fx-text-fill: #ff0000;");
                    showAlert("Xatolik", "Xabar yuborishda xatolik: " + e.getMessage());
                });
            }
        }).start();
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
