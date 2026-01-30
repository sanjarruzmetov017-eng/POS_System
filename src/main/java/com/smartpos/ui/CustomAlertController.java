package com.smartpos.ui;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;

@Component
@Scope("prototype")
public class CustomAlertController {

    @FXML
    private Label titleLabel;
    @FXML
    private Label messageLabel;
    @FXML
    private FontAwesomeIconView iconView;

    public void setAlertData(String title, String message, AlertType type) {
        titleLabel.setText(title);
        messageLabel.setText(message);

        switch (type) {
            case SUCCESS:
                iconView.setIcon(FontAwesomeIcon.CHECK_CIRCLE);
                iconView.getStyleClass().add("alert-icon-success");
                break;
            case ERROR:
                iconView.setIcon(FontAwesomeIcon.EXCLAMATION_CIRCLE);
                iconView.getStyleClass().add("alert-icon-error");
                break;
            case WARNING:
                iconView.setIcon(FontAwesomeIcon.EXCLAMATION_TRIANGLE);
                iconView.getStyleClass().add("alert-icon-warning");
                break;
            case INFO:
                iconView.setIcon(FontAwesomeIcon.INFO_CIRCLE);
                iconView.getStyleClass().add("alert-icon-info");
                break;
        }
    }

    @FXML
    private void handleClose() {
        ((Stage) titleLabel.getScene().getWindow()).close();
    }

    public enum AlertType {
        SUCCESS, ERROR, WARNING, INFO
    }
}
