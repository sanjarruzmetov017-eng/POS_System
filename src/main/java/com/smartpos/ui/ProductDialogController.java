package com.smartpos.ui;

import com.smartpos.model.Category;
import com.smartpos.model.Product;
import com.smartpos.service.CategoryService;
import com.smartpos.service.ProductService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Scope("prototype")
public class ProductDialogController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @FXML
    private TextField nameField;
    @FXML
    private TextField barcodeField;
    @FXML
    private ComboBox<Category> categoryCombo;
    @FXML
    private TextField priceField;
    @FXML
    private TextField costPriceField;
    @FXML
    private TextField stockField;
    @FXML
    private ComboBox<Product.UnitType> unitCombo;

    private Product product;
    private boolean saved = false;

    @FXML
    public void initialize() {
        // Load Categories
        List<Category> categories = categoryService.findAll();
        categoryCombo.setItems(FXCollections.observableArrayList(categories));
        categoryCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getName());
            }
        });
        categoryCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? "" : item.getName());
            }
        });

        // Load Unit Types
        unitCombo.setItems(FXCollections.observableArrayList(Product.UnitType.values()));
        unitCombo.setValue(Product.UnitType.PIECE);
    }

    public void setProduct(Product product) {
        this.product = product;
        if (product != null && product.getId() != null) {
            nameField.setText(product.getName());
            barcodeField.setText(product.getBarcode());
            categoryCombo.setValue(product.getCategory());
            priceField.setText(product.getPrice().toString());
            costPriceField.setText(product.getCostPrice().toString());
            stockField.setText(product.getStockQuantity().toString());
            unitCombo.setValue(product.getUnitType());
        }
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    private void handleGenerateBarcode() {
        barcodeField.setText(String.valueOf(System.currentTimeMillis()));
    }

    @FXML
    private void handleSave() {
        if (!validateInput())
            return;

        if (product == null) {
            product = new Product();
        }

        product.setName(nameField.getText().trim());
        product.setBarcode(barcodeField.getText().trim());
        product.setCategory(categoryCombo.getValue());
        product.setPrice(new BigDecimal(priceField.getText()));
        product.setCostPrice(new BigDecimal(costPriceField.getText()));
        product.setStockQuantity(new BigDecimal(stockField.getText()));
        product.setUnitType(unitCombo.getValue());

        try {
            productService.save(product);
            saved = true;
            closeStage();
        } catch (Exception e) {
            showAlert("Xatolik", "Mahsulotni saqlashda xato yuz berdi: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeStage();
    }

    private boolean validateInput() {
        String msg = "";
        if (nameField.getText().isEmpty())
            msg += "Nom kiriting!\n";
        if (priceField.getText().isEmpty())
            msg += "Sotuv narxini kiriting!\n";
        if (costPriceField.getText().isEmpty())
            msg += "Tannarxni kiriting!\n";
        if (stockField.getText().isEmpty())
            msg += "Zaxira miqdorini kiriting!\n";

        try {
            new BigDecimal(priceField.getText());
            new BigDecimal(costPriceField.getText());
            new BigDecimal(stockField.getText());
        } catch (Exception e) {
            msg += "Narx va zaxira raqam bo'lishi shart!\n";
        }

        if (!msg.isEmpty()) {
            showAlert("Maydonlarni to'ldiring", msg);
            return false;
        }
        return true;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closeStage() {
        ((Stage) nameField.getScene().getWindow()).close();
    }
}
