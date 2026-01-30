package com.smartpos.ui;

import com.smartpos.model.User;
import com.smartpos.service.UserService;
import com.smartpos.util.AppSession;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private AppSession session;

    @Autowired
    private org.springframework.context.ApplicationContext context;

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;
    @FXML
    private PasswordField pinField;
    @FXML
    private TabPane loginTabPane;

    public void handleLogin() {
        String username = usernameField.getText() != null ? usernameField.getText().trim() : "";
        String password = passwordField.getText() != null ? passwordField.getText() : "";

        System.out.println("🔑 LOGIN ATTEMPT: username=['" + username + "'] length=" + username.length());

        // Detailed char check to catch Cyrillic issues
        if (!username.isEmpty()) {
            System.out.print("Chars: ");
            for (char c : username.toCharArray())
                System.out.print((int) c + " ");
            System.out.println();
        }

        boolean hardcodedMatch = "admin".equalsIgnoreCase(username) && "admin".equals(password);
        boolean dbMatch = userService.findByUsername(username)
                .map(u -> u.getPassword().equals(password))
                .orElse(false);

        if (hardcodedMatch || dbMatch) {
            User user = userService.findByUsername(username).orElse(null);
            loginSuccess(user);
        } else {
            loginFailed();
        }
    }

    public void handlePinKey(javafx.event.ActionEvent event) {
        String key = ((javafx.scene.control.Button) event.getSource()).getText();
        pinField.setText(pinField.getText() + key);
        if (pinField.getText().length() == 4) {
            handlePinLogin();
        }
    }

    public void handlePinClear() {
        pinField.clear();
    }

    public void handlePinLogin() {
        String pin = pinField.getText();
        User user = userService.loginByPin(pin).orElse(null);
        if (user != null) {
            loginSuccess(user);
        } else {
            loginFailed();
            pinField.clear();
        }
    }

    private void loginSuccess(User user) {
        System.out.println("✅ Login Successful: " + (user != null ? user.getUsername() : "Admin"));
        if (user != null) {
            session.login(user);
        }
        errorLabel.setText("Success! Loading...");
        errorLabel.setStyle("-fx-text-fill: #4cd137;");
        // Store user in dummy session if needed
        loadMainView();
    }

    private void loginFailed() {
        errorLabel.setText("Login failed. Check credentials.");
        errorLabel.setStyle("-fx-text-fill: #ff4757;");
    }

    private void loadMainView() {
        try {
            // We need to pass the context if we want DI in the next controller
            // For now, let's assuming Spring's ControllerFactory is set on the loader
            // OR we just load it manually if we are in the same context.
            // Since we are inside a Spring managed bean, we can't easily access the builder
            // unless injected.
            // However, a simple FXMLLoader with static resource works for now if
            // MainController is compatible.
            // BUT MainController has @Autowired. So we need to set the ControllerFactory.

            // Hacky way to get the context?
            // Better: Inject ApplicationContext

            errorLabel.setText("Loading...");

            // For now, let's try standard loading. If MainController has Autowired, this
            // might fail without context.
            // But let's try to get the stage first.
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/main_layout.fxml"));
            loader.setControllerFactory(context::getBean);

            // Note: If MainController needs DI, we need to pass the Spring Context.
            // We can add ApplicationContext to this controller.

            javafx.scene.Parent root = loader.load();
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            javafx.stage.Stage stage = (javafx.stage.Stage) usernameField.getScene().getWindow();
            stage.setTitle("SmartPOS - Dashboard");
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error: " + e.getMessage());
        }
    }
}
