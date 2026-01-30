package com.smartpos.ui;

import com.smartpos.model.Category;
import com.smartpos.model.Product;
import com.smartpos.service.CategoryService;
import com.smartpos.service.ProductService;
import com.smartpos.util.AppSession;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Component
@Scope("prototype")
public class ProductDialogController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AppSession session;

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
    @FXML
    private TextField thresholdField;
    @FXML
    private ImageView productImageView;
    @FXML
    private Label imagePlaceholderLabel;

    private Product product;
    private File selectedImageFile;
    private boolean saved = false;

    private static final String IMAGE_DIR = "data/product-images/";

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
            thresholdField.setText(
                    product.getLowStockThreshold() != null ? product.getLowStockThreshold().toString() : "0.00");

            if (product.getImageUrl() != null) {
                loadImage(product.getImageUrl());
            }
        }
    }

    private void loadImage(String url) {
        try {
            File file = new File(url);
            if (file.exists()) {
                productImageView.setImage(new Image(file.toURI().toString()));
                imagePlaceholderLabel.setVisible(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
    private void handleSelectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Rasm tanlash");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Rasmlar", "*.png", "*.jpg", "*.jpeg", "*.webp"));

        File file = fileChooser.showOpenDialog(nameField.getScene().getWindow());
        if (file != null) {
            selectedImageFile = file;
            productImageView.setImage(new Image(file.toURI().toString()));
            imagePlaceholderLabel.setVisible(false);
        }
    }

    @FXML
    private void handleRemoveImage() {
        selectedImageFile = null;
        productImageView.setImage(null);
        imagePlaceholderLabel.setVisible(true);
        if (product != null) {
            product.setImageUrl(null);
        }
    }

    @FXML
    private void handleSave() {
        if (!validateInput())
            return;

        if (session.getCurrentTenant() == null) {
            showAlert("Xatolik", "Seans muddati tugagan yoki do'kon ma'lumoti topilmadi. Iltimos, qaytadan kiring.");
            return;
        }

        if (product == null) {
            product = new Product();
        }

        product.setName(nameField.getText().trim());

        // Handle Barcode (Treat empty as null to avoid unique constraint issues)
        String barcode = barcodeField.getText().trim();
        product.setBarcode(barcode.isEmpty() ? null : barcode);

        product.setCategory(categoryCombo.getValue());
        product.setPrice(new BigDecimal(priceField.getText()));
        product.setCostPrice(new BigDecimal(costPriceField.getText()));
        product.setStockQuantity(new BigDecimal(stockField.getText()));
        product.setUnitType(unitCombo.getValue());

        String thresholdStr = thresholdField.getText().trim();
        product.setLowStockThreshold(thresholdStr.isEmpty() ? BigDecimal.ZERO : new BigDecimal(thresholdStr));

        // Handle Image Save
        if (selectedImageFile != null) {
            String savedPath = saveImage(selectedImageFile);
            if (savedPath != null) {
                product.setImageUrl(savedPath);
            }
        }

        try {
            productService.save(product);
            saved = true;
            closeStage();
        } catch (Exception e) {
            e.printStackTrace(); // Log stack trace
            showAlert("Xatolik", "Mahsulotni saqlashda xato yuz berdi: " +
                    (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
        }
    }

    private String saveImage(File file) {
        try {
            Path dir = Paths.get(IMAGE_DIR);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            String fileName = System.currentTimeMillis() + "_" + file.getName();
            Path target = dir.resolve(fileName);
            Files.copy(file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);

            return target.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
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
            if (!thresholdField.getText().trim().isEmpty()) {
                new BigDecimal(thresholdField.getText().trim());
            }
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
