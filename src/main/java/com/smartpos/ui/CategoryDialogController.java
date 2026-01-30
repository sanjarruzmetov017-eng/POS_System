package com.smartpos.ui;

import com.smartpos.model.Category;
import com.smartpos.service.CategoryService;
import com.smartpos.util.AppSession;
import com.smartpos.util.NotificationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class CategoryDialogController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AppSession session;

    @Autowired
    private NotificationUtil notificationUtil;

    @FXML
    private TextField nameField;

    private Category category;
    private boolean saved = false;

    public void setCategory(Category category) {
        this.category = category;
        if (category != null && category.getId() != null) {
            nameField.setText(category.getName());
        }
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void handleSave() {
        if (nameField.getText().trim().isEmpty()) {
            showAlert("Xatolik", "Kategoriya nomini kiriting!");
            return;
        }

        if (session.getCurrentTenant() == null) {
            showAlert("Xatolik", "Seans muddati tugagan yoki do'kon ma'lumoti topilmadi. Iltimos, qaytadan kiring.");
            return;
        }

        if (category == null) {
            category = new Category();
        }

        category.setName(nameField.getText().trim());

        try {
            categoryService.save(category);
            saved = true;
            closeStage();
        } catch (Exception e) {
            e.printStackTrace(); // Log stack trace
            showAlert("Xatolik", "Saqlashda xato: " +
                    (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
        }
    }

    @FXML
    private void handleCancel() {
        closeStage();
    }

    private void showAlert(String title, String content) {
        if ("Xatolik".equalsIgnoreCase(title)) {
            notificationUtil.showError(title, content);
        } else {
            notificationUtil.showInfo(title, content);
        }
    }

    private void closeStage() {
        ((Stage) nameField.getScene().getWindow()).close();
    }
}
