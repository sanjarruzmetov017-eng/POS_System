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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class ProductViewController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @FXML
    private TextField searchField;
    @FXML
    private TableView<Product> productTable;
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
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));

        colCategory.setCellValueFactory(cellData -> {
            Category cat = cellData.getValue().getCategory();
            return new SimpleStringProperty(cat != null ? cat.getName() : "-");
        });

        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));

        // Action Column with Delete Button
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Delete");

            {
                btn.getStyleClass().add("nav-button-critical");
                btn.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    productService.delete(product.getId());
                    loadProducts();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });
    }

    private void loadProducts() {
        List<Product> products = productService.findAll();
        productTable.setItems(FXCollections.observableArrayList(products));
    }

    @FXML
    public void handleNewProduct() {
        // Todo: Open Dialog
        System.out.println("Open New Product Dialog");

        // Temporary: Create a dummy product to verify list updates
        Product p = new Product();
        p.setName("New Product " + System.currentTimeMillis());
        p.setPrice(new BigDecimal("99.99"));
        p.setCostPrice(new BigDecimal("50.00"));
        p.setStockQuantity(new BigDecimal("100"));
        productService.save(p);
        loadProducts();
    }
}
