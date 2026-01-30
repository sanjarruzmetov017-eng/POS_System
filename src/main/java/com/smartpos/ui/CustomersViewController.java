package com.smartpos.ui;

import com.smartpos.model.Customer;
import com.smartpos.service.CustomerService;
import com.smartpos.util.NotificationUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CustomersViewController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private NotificationUtil notificationUtil;

    @FXML
    private TextField searchField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField emailField;

    @FXML
    private TableView<Customer> customerTable;
    @FXML
    private TableColumn<Customer, Long> colId;
    @FXML
    private TableColumn<Customer, String> colName;
    @FXML
    private TableColumn<Customer, String> colPhone;
    @FXML
    private TableColumn<Customer, String> colEmail;
    @FXML
    private TableColumn<Customer, Integer> colLoyalty;
    @FXML
    private TableColumn<Customer, Void> colAction;

    @FXML
    public void initialize() {
        setupTable();
        loadCustomers();

        searchField.textProperty().addListener((obs, old, newVal) -> filterCustomers(newVal));
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colLoyalty.setCellValueFactory(new PropertyValueFactory<>("loyaltyPoints"));

        // Action Buttons (Delete/Edit) - Simplified for now
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Delete");
            {
                btn.getStyleClass().add("button-danger");
                btn.setOnAction(event -> {
                    // Implement delete Logic (requires Service delete method)
                    // For now just alert
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Delete feature coming soon", ButtonType.OK);
                    alert.show();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    private void loadCustomers() {
        customerTable.setItems(FXCollections.observableArrayList(customerService.findAll()));
    }

    private void filterCustomers(String query) {
        if (query == null || query.isEmpty()) {
            loadCustomers();
            return;
        }
        ObservableList<Customer> filtered = FXCollections.observableArrayList();
        for (Customer c : customerService.findAll()) {
            if (c.getName().toLowerCase().contains(query.toLowerCase()) ||
                    (c.getPhone() != null && c.getPhone().contains(query))) {
                filtered.add(c);
            }
        }
        customerTable.setItems(filtered);
    }

    @FXML
    public void handleAddCustomer() {
        String name = nameField.getText();
        String phone = phoneField.getText();
        String email = emailField.getText();

        if (name == null || name.isEmpty()) {
            showAlert("Error", "Name is required");
            return;
        }

        Customer customer = new Customer();
        customer.setName(name);
        customer.setPhone(phone);
        customer.setEmail(email);
        customer.setLoyaltyPoints(0);

        customerService.save(customer);

        // Clear fields
        nameField.clear();
        phoneField.clear();
        emailField.clear();

        loadCustomers();
        showAlert("Success", "Customer added successfully");
    }

    private void showAlert(String title, String content) {
        if ("Success".equalsIgnoreCase(title)) {
            notificationUtil.showSuccess(title, content);
        } else if ("Error".equalsIgnoreCase(title)) {
            notificationUtil.showError(title, content);
        } else {
            notificationUtil.showInfo(title, content);
        }
    }
}
