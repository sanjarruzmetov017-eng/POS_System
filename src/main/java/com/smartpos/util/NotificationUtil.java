package com.smartpos.util;

import com.smartpos.ui.CustomAlertController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class NotificationUtil {

    @Autowired
    private ApplicationContext context;

    public void show(String title, String message, CustomAlertController.AlertType type) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/custom_alert.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();

            CustomAlertController controller = loader.getController();
            controller.setAlertData(title, message, type);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);

            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to standard alert if custom fails
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.show();
        }
    }

    public void showSuccess(String title, String message) {
        show(title, message, CustomAlertController.AlertType.SUCCESS);
    }

    public void showError(String title, String message) {
        show(title, message, CustomAlertController.AlertType.ERROR);
    }

    public void showWarning(String title, String message) {
        show(title, message, CustomAlertController.AlertType.WARNING);
    }

    public void showInfo(String title, String message) {
        show(title, message, CustomAlertController.AlertType.INFO);
    }
}
