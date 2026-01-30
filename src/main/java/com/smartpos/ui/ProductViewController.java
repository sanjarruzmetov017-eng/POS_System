package com.smartpos.ui;

import com.smartpos.model.Category;
import com.smartpos.model.Product;
import com.smartpos.service.ProductService;
import com.smartpos.service.CategoryService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.fxml.FXMLLoader;
import java.io.File;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;

import java.math.BigDecimal;
import java.util.List;

@Component
public class ProductViewController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ApplicationContext applicationContext;

    @FXML
    private TextField searchField;
    @FXML
    private TableView<Product> productTable;
    @FXML
    private TableColumn<Product, String> colImage;
    @FXML
    private TableColumn<Product, Long> colId;
    @FXML
    private TableColumn<Product, String> colName;
    @FXML
    private TableColumn<Product, String> colCategory;
    @FXML
    private TableColumn<Product, BigDecimal> colPrice;
    @FXML
    private TableColumn<Product, BigDecimal> colStock;
    @FXML
    private TableColumn<Product, Void> colAction;

    @FXML
    public void initialize() {
        setupTable();
        loadProducts();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                loadProducts();
            } else {
                productTable.setItems(FXCollections.observableArrayList(productService.search(newValue)));
            }
        });
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        colImage.setCellFactory(param -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitWidth(40);
                imageView.setFitHeight(40);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Product p = getTableRow().getItem();
                    if (p.getImageUrl() != null) {
                        try {
                            File file = new File(p.getImageUrl());
                            if (file.exists()) {
                                imageView.setImage(new Image(file.toURI().toString(), true));
                                setGraphic(imageView);
                                alignmentProperty().set(Pos.CENTER);
                            } else {
                                setGraphic(null);
                            }
                        } catch (Exception e) {
                            setGraphic(null);
                        }
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        colName.setCellValueFactory(new PropertyValueFactory<>("name"));

        colCategory.setCellValueFactory(cellData -> {
            Category cat = cellData.getValue().getCategory();
            return new SimpleStringProperty(cat != null ? cat.getName() : "-");
        });

        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));

        // Action Column with Edit and Delete Buttons
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox container = new HBox(10, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("button-secondary");
                editBtn.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    showProductDialog(product);
                });

                deleteBtn.getStyleClass().add("nav-button-critical");
                deleteBtn.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    productService.delete(product.getId());
                    loadProducts();
                });

                container.setAlignment(javafx.geometry.Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        });
    }

    private void loadProducts() {
        List<Product> products = productService.findAll();
        productTable.setItems(FXCollections.observableArrayList(products));
    }

    @FXML
    private void handleManageCategories() {
        loadView("/fxml/categories_view.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(applicationContext::getBean);
            Parent view = loader.load();
            ((StackPane) productTable.getScene().lookup("#contentArea")).getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleNewProduct() {
        showProductDialog(null);
    }

    private void showProductDialog(Product product) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/product_dialog.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            ProductDialogController controller = loader.getController();
            controller.setProduct(product);

            Stage stage = new Stage();
            stage.setTitle(product == null ? "Yangi mahsulot" : "Mahsulotni tahrirlash");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            if (controller.isSaved()) {
                loadProducts();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Xatolik");
            alert.setContentText("Muloqot oynasini ochib bo'lmadi: " + e.getMessage());
            alert.show();
        }
    }
}
