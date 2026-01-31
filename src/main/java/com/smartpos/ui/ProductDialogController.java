package com.smartpos.ui;

import com.smartpos.model.Category;
import com.smartpos.model.Product;
import com.smartpos.service.CategoryService;
import com.smartpos.service.ProductService;
import com.smartpos.util.AppSession;
import com.smartpos.util.NotificationUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.geometry.Pos;
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

    @Autowired
    private NotificationUtil notificationUtil;

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
    private VBox imageFlowPane;
    @FXML
    private StackPane imagePlaceholder;

    private Product product;
    private java.util.List<File> newImageFiles = new java.util.ArrayList<>();
    private java.util.List<String> existingImageUrls = new java.util.ArrayList<>();
    private boolean saved = false;

    private static final String IMAGE_DIR = System.getProperty("user.home") + File.separator + ".smartpos"
            + File.separator + "product-images" + File.separator;

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

            if (product.getImageUrls() != null) {
                existingImageUrls.addAll(product.getImageUrls());
                refreshImageUI();
            }
        }
    }

    private void refreshImageUI() {
        imageFlowPane.getChildren().clear();
        imagePlaceholder.setVisible(existingImageUrls.isEmpty() && newImageFiles.isEmpty());

        // Existing
        for (String url : existingImageUrls) {
            addImageThumbnail(url, false);
        }
        // New
        for (File file : newImageFiles) {
            addImageThumbnail(file.toURI().toString(), true);
        }
    }

    private void addImageThumbnail(String source, boolean isNew) {
        StackPane container = new StackPane();
        container.getStyleClass().add("glass-pane");
        container.setPadding(new javafx.geometry.Insets(5));

        ImageView iv = new ImageView(new Image(source, 100, 100, true, true));
        iv.setFitWidth(100);
        iv.setFitHeight(100);
        iv.setPreserveRatio(true);

        Button removeBtn = new Button();
        removeBtn.setGraphic(new de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView(
                de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon.TRASH));
        removeBtn.getStyleClass().add("button-secondary");
        removeBtn.setStyle("-fx-background-color: rgba(245, 87, 108, 0.8); -fx-text-fill: white; -fx-padding: 2;");
        StackPane.setAlignment(removeBtn, Pos.TOP_RIGHT);

        removeBtn.setOnAction(e -> {
            if (isNew) {
                newImageFiles.removeIf(f -> f.toURI().toString().equals(source));
            } else {
                existingImageUrls.remove(source);
            }
            refreshImageUI();
        });

        container.getChildren().addAll(iv, removeBtn);
        imageFlowPane.getChildren().add(container);
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

        List<File> files = fileChooser.showOpenMultipleDialog(nameField.getScene().getWindow());
        if (files != null) {
            newImageFiles.addAll(files);
            refreshImageUI();
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

        // Handle Barcode
        String barcode = barcodeField.getText().trim();
        product.setBarcode(barcode.isEmpty() ? null : barcode);

        product.setCategory(categoryCombo.getValue());
        product.setPrice(parseBigDecimal(priceField.getText()));
        product.setCostPrice(parseBigDecimal(costPriceField.getText()));
        product.setStockQuantity(parseBigDecimal(stockField.getText()));
        product.setUnitType(unitCombo.getValue());

        String thresholdStr = thresholdField.getText().trim();
        product.setLowStockThreshold(thresholdStr.isEmpty() ? BigDecimal.ZERO : parseBigDecimal(thresholdStr));

        // Handle Images
        java.util.List<String> finalUrls = new java.util.ArrayList<>(existingImageUrls);
        for (File file : newImageFiles) {
            String savedPath = saveImage(file);
            if (savedPath != null) {
                finalUrls.add(savedPath);
            }
        }
        product.setImageUrls(finalUrls);

        try {
            productService.save(product);
            saved = true;
            closeStage();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Xatolik", "Mahsulotni saqlashda xato yuz berdi: " +
                    (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
        }
    }

    private BigDecimal parseBigDecimal(String str) {
        if (str == null || str.isEmpty())
            return BigDecimal.ZERO;
        return new BigDecimal(str.replace(",", "."));
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
            System.err.println("❌ Error saving image: " + e.getMessage());
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
            parseBigDecimal(priceField.getText());
            parseBigDecimal(costPriceField.getText());
            parseBigDecimal(stockField.getText());
            if (!thresholdField.getText().trim().isEmpty()) {
                parseBigDecimal(thresholdField.getText().trim());
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
        if ("Xatolik".equalsIgnoreCase(title)) {
            notificationUtil.showError(title, content);
        } else if ("Ogohlantirish".equalsIgnoreCase(title)) {
            notificationUtil.showWarning(title, content);
        } else {
            notificationUtil.showInfo(title, content);
        }
    }

    private void closeStage() {
        ((Stage) nameField.getScene().getWindow()).close();
    }
}
