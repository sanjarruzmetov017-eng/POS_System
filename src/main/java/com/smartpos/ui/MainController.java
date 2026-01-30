package com.smartpos.ui;

import com.smartpos.service.ConnectivityService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.context.ApplicationContext;
import com.smartpos.util.AppSession;
import javafx.scene.control.Button;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

@Component
public class MainController {

    @Autowired
    private ApplicationContext context;

    @FXML
    private StackPane contentArea;

    @Autowired
    private ConnectivityService connectivityService;

    @Autowired
    private com.smartpos.service.StockAlertService stockAlertService;

    @Autowired
    private com.smartpos.service.CashRegisterService cashRegisterService;

    @Autowired
    private com.smartpos.service.UserService userService;

    @FXML
    private Circle statusCircle;

    @FXML
    private Label statusLabel;

    @FXML
    private javafx.scene.layout.HBox lowStockBadge;

    @FXML
    private Label lowStockLabel;

    @Autowired
    private AppSession session;

    @FXML
    private Button productsBtn;
    @FXML
    private Button inventoryBtn;
    @FXML
    private Button expensesBtn;
    @FXML
    private Button reportsBtn;
    @FXML
    private Button settingsBtn;
    @FXML
    private Button adminBtn;
    @FXML
    private Button shiftsBtn;
    @FXML
    private Button superAdminBtn;
    @FXML
    private Button closeRegisterBtn;

    @FXML
    private javafx.scene.control.TableView<?> recentSalesTable;

    @FXML
    private Label storeNameLabel;

    @FXML
    public void initialize() {
        // Load default view
        showDashboard();

        // RBAC Enforcement
        applyRbac();

        // Set Store Name from Tenant
        if (session.getCurrentTenant() != null) {
            storeNameLabel.setText(session.getCurrentTenant().getName().toUpperCase());
        }

        // Bind connectivity status
        connectivityService.onlineProperty().addListener((obs, oldValue, newValue) -> {
            updateStatusUI(newValue);
        });
        updateStatusUI(connectivityService.isOnline());

        // Bind low stock alert
        stockAlertService.lowStockCountProperty().addListener((obs, old, newVal) -> {
            updateLowStockUI(newVal.intValue());
        });
        updateLowStockUI(stockAlertService.getLowStockCount());
        stockAlertService.checkStock(); // Initial check
    }

    private void checkActiveSession() {
        if (cashRegisterService.getActiveSession().isEmpty()) {
            showOpenRegisterDialog();
        }
    }

    private void showOpenRegisterDialog() {
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog("0.00");
        dialog.setTitle("Open Register");
        dialog.setHeaderText("No active session found.");
        dialog.setContentText("Enter starting cash amount:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String amount = result.get();
            try {
                BigDecimal startCash = new BigDecimal(amount);
                java.util.List<com.smartpos.model.User> users = userService.findAll();
                if (users.isEmpty()) {
                    showErrorAlert("Registration Error", "No users found in database. Please contact admin.");
                    return;
                }
                com.smartpos.model.User currentUser = users.get(0);
                cashRegisterService.openSession(currentUser, startCash);
                showDashboard();
            } catch (NumberFormatException e) {
                showErrorAlert("Invalid Amount", "Please enter a valid numeric amount.");
                showOpenRegisterDialog(); // Reprompt for invalid input only
            } catch (Exception e) {
                e.printStackTrace();
                showErrorAlert("System Error", "Could not open register: " + e.getMessage());
            }
        }
    }

    private void showErrorAlert(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void updateLowStockUI(int count) {
        if (count > 0) {
            lowStockLabel.setText(String.valueOf(count));
            lowStockBadge.setVisible(true);
        } else {
            lowStockBadge.setVisible(false);
        }
    }

    private void applyRbac() {
        // Super Admin check: only "admin" user or SUPER_ADMIN role can see the global
        // management
        boolean isSuperUser = "admin".equalsIgnoreCase(session.getCurrentUser().getUsername());
        superAdminBtn.setVisible(isSuperUser);
        superAdminBtn.setManaged(isSuperUser);

        if (!session.isAdmin()) {
            // Cashier restrictions - Hide sensitive modules completely
            productsBtn.setVisible(false);
            productsBtn.setManaged(false);

            inventoryBtn.setVisible(false);
            inventoryBtn.setManaged(false);

            expensesBtn.setVisible(false);
            expensesBtn.setManaged(false);

            reportsBtn.setVisible(false);
            reportsBtn.setManaged(false);

            settingsBtn.setVisible(false);
            settingsBtn.setManaged(false);

            adminBtn.setVisible(false);
            adminBtn.setManaged(false);

            System.out.println("🛡️ RBAC: Active for Cashier - sensitive modules hidden.");
        } else {
            adminBtn.setVisible(true);
            adminBtn.setManaged(true);
            System.out.println("🛡️ RBAC: Admin session - all modules enabled.");
        }
    }

    private void updateStatusUI(boolean isOnline) {
        if (isOnline) {
            statusCircle.setFill(Color.web("#00ff88"));
            statusLabel.setText("ONLINE");
        } else {
            statusCircle.setFill(Color.web("#ff4b2b"));
            statusLabel.setText("OFFLINE");
        }
    }

    @FXML
    public void showDashboard() {
        loadView("/fxml/dashboard_view.fxml");
    }

    @FXML
    public void showSales() {
        if (cashRegisterService.getActiveSession().isEmpty()) {
            showOpenRegisterDialog();
        }
        if (cashRegisterService.getActiveSession().isPresent()) {
            loadView("/fxml/sales_view.fxml");
        }
    }

    @FXML
    public void showProducts() {
        loadView("/fxml/products_view.fxml");
    }

    @FXML
    public void showCustomers() {
        loadView("/fxml/customers_view.fxml");
    }

    @FXML
    public void showInventory() {
        loadView("/fxml/inventory_view.fxml");
    }

    @FXML
    public void showExpenses() {
        loadView("/fxml/expenses_view.fxml");
    }

    @FXML
    public void showReports() {
        loadView("/fxml/reports_view.fxml");
    }

    @FXML
    public void showSettings() {
        loadView("/fxml/settings_view.fxml");
    }

    @FXML
    public void showAdmin() {
        loadView("/fxml/admin_view.fxml");
    }

    @FXML
    public void showShifts() {
        loadView("/fxml/shifts_view.fxml");
    }

    @FXML
    public void showSuperAdmin() {
        loadView("/fxml/super_admin_view.fxml");
    }

    @FXML
    public void handleCloseRegister() {
        if (cashRegisterService.getActiveSession().isEmpty()) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("No active session");
            alert.setContentText("The register is already closed.");
            alert.show();
            return;
        }
        showCloseRegisterDialog();
    }

    private void showCloseRegisterDialog() {
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog("0.00");
        dialog.setTitle("Close Register (End of Day)");
        dialog.setHeaderText("Closing the register session.");
        dialog.setContentText("Enter actual cash amount in drawer:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(amount -> {
            try {
                BigDecimal endCash = new BigDecimal(amount);
                com.smartpos.model.CashSession session = cashRegisterService.closeSession(endCash,
                        "End of day closure");

                String summary = String.format(
                        "Session Summary:\nStart Cash: %.2f\nExpected Cash: %.2f\nActual Cash: %.2f\nDiscrepancy: %.2f",
                        session.getStartCash(), session.getExpectedEndCash(), session.getEndCash(),
                        session.getDiscrepancy());

                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("Register Closed");
                alert.setHeaderText("Shift Summary");
                alert.setContentText(summary);
                alert.showAndWait();

                showDashboard();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    public void handleLogout() {
        try {
            session.logout();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login_layout.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) contentArea.getScene().getWindow();
            stage.setTitle("SmartPOS - Login");
            stage.setScene(new javafx.scene.Scene(root));
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(context::getBean);
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
