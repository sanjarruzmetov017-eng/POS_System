package com.smartpos.ui;

import com.smartpos.model.Category;
import com.smartpos.service.CategoryService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class CategoryViewController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ApplicationContext applicationContext;

    @FXML
    private TableView<Category> categoryTable;
    @FXML
    private TableColumn<Category, Long> colId;
    @FXML
    private TableColumn<Category, String> colName;
    @FXML
    private TableColumn<Category, Void> colAction;

    @FXML
    public void initialize() {
        setupTable();
        loadCategories();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));

        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Tahrirlash");
            private final Button deleteBtn = new Button("O'chirish");
            private final HBox container = new HBox(10, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("button-secondary");
                editBtn.setOnAction(event -> {
                    Category category = getTableView().getItems().get(getIndex());
                    showCategoryDialog(category);
                });

                deleteBtn.getStyleClass().add("nav-button-critical");
                deleteBtn.setOnAction(event -> {
                    Category category = getTableView().getItems().get(getIndex());
                    try {
                        categoryService.delete(category.getId());
                        loadCategories();
                    } catch (Exception e) {
                        showAlert("Xatolik",
                                "Kategoriyani o'chirib bo'lmaydi. Balki u mahsulotlarga biriktirilgandir.");
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void loadCategories() {
        List<Category> categories = categoryService.findAll();
        categoryTable.setItems(FXCollections.observableArrayList(categories));
    }

    @FXML
    private void handleNewCategory() {
        showCategoryDialog(null);
    }

    @FXML
    private void handleBack() {
        // This is handled by MainController when it loads the view,
        // but for now let's just trigger a reload of product view if we came from there
        loadView("/fxml/products_view.fxml");
    }

    private void showCategoryDialog(Category category) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/category_dialog.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            CategoryDialogController controller = loader.getController();
            controller.setCategory(category);

            Stage stage = new Stage();
            stage.setTitle(category == null ? "Yangi Kategoriya" : "Kategoriyani Tahrirlash");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(categoryTable.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (controller.isSaved()) {
                loadCategories();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(applicationContext::getBean);
            Parent view = loader.load();
            ((StackPane) categoryTable.getScene().lookup("#contentArea")).getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
