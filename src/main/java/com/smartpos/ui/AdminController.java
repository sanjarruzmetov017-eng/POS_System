package com.smartpos.ui;

import com.smartpos.model.User;
import com.smartpos.service.UserService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AdminController {

    @Autowired
    private UserService userService;

    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, String> colUsername;
    @FXML
    private TableColumn<User, String> colFullName;
    @FXML
    private TableColumn<User, User.Role> colRole;
    @FXML
    private TableColumn<User, Boolean> colActive;

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField pinField;
    @FXML
    private ComboBox<User.Role> roleComboBox;
    @FXML
    private CheckBox activeCheckBox;

    @FXML
    public void initialize() {
        setupTable();
        loadUsers();
        roleComboBox.setItems(FXCollections.observableArrayList(User.Role.values()));
        roleComboBox.getSelectionModel().select(User.Role.CASHIER);
        activeCheckBox.setSelected(true);
    }

    private void setupTable() {
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));

        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                populateFields(newV);
            }
        });
    }

    private void loadUsers() {
        userTable.setItems(FXCollections.observableArrayList(userService.findAll()));
    }

    private void populateFields(User user) {
        usernameField.setText(user.getUsername());
        fullNameField.setText(user.getFullName());
        pinField.setText(user.getPin());
        roleComboBox.setValue(user.getRole());
        activeCheckBox.setSelected(user.isActive());
        passwordField.clear(); // Safety: don't show existing password
    }

    @FXML
    public void handleSave() {
        String username = usernameField.getText();
        if (username == null || username.isEmpty())
            return;

        User user = userService.findByUsername(username).orElse(new User());
        user.setUsername(username);
        user.setFullName(fullNameField.getText());
        user.setPin(pinField.getText());
        user.setRole(roleComboBox.getValue());
        user.setActive(activeCheckBox.isSelected());

        if (passwordField.getText() != null && !passwordField.getText().isEmpty()) {
            user.setPassword(passwordField.getText());
        } else if (user.getId() == null) {
            // New user must have password
            showAlert("Xato", "Yangi xodim uchun parol kiritilishi shart.");
            return;
        }

        userService.save(user);
        loadUsers();
        clearFields();
        showAlert("Muvaffaqiyat", "Xodim ma'lumotlari saqlandi.");
    }

    @FXML
    public void handleDelete() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if ("admin".equals(selected.getUsername())) {
                showAlert("Taqiq", "Asosiy adminni o'chirib bo'lmaydi.");
                return;
            }
            userService.delete(selected.getId());
            loadUsers();
            clearFields();
        }
    }

    @FXML
    public void clearFields() {
        userTable.getSelectionModel().clearSelection();
        usernameField.clear();
        passwordField.clear();
        fullNameField.clear();
        pinField.clear();
        roleComboBox.getSelectionModel().select(User.Role.CASHIER);
        activeCheckBox.setSelected(true);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}
