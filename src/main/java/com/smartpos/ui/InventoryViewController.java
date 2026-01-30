package com.smartpos.ui;

import com.smartpos.model.Product;
import com.smartpos.service.ProductService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class InventoryViewController {

    @Autowired
    private ProductService productService;

    @Autowired
    private com.smartpos.repository.PriceHistoryRepository priceHistoryRepository;

    @Autowired
    private com.smartpos.service.AiService aiService;

    @Autowired
    private com.smartpos.service.BarcodeService barcodeService;

    @FXML
    private TableView<Product> inventoryTable;
    @FXML
    private TableColumn<Product, String> colName;
    @FXML
    private TableColumn<Product, String> colBarcode;
    @FXML
    private TableColumn<Product, java.math.BigDecimal> colStock;
    @FXML
    private TableColumn<Product, BigDecimal> colThreshold;
    @FXML
    private TableColumn<Product, String> colForecast;
    @FXML
    private TableColumn<Product, Void> colAction;

    @FXML
    private Label totalItemsLabel;
    @FXML
    private Label lowStockLabel;
    @FXML
    private TextField searchField;

    @FXML
    public void initialize() {
        setupTable();
        loadInventory();

        searchField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                loadInventory();
            } else {
                inventoryTable.setItems(FXCollections.observableArrayList(productService.search(newVal)));
            }
        });
    }

    private void setupTable() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colBarcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));

        colStock.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        colStock.setCellFactory(
                TextFieldTableCell.forTableColumn(new javafx.util.converter.BigDecimalStringConverter()));
        colStock.setOnEditCommit(event -> {
            Product product = event.getRowValue();
            product.setStockQuantity(event.getNewValue());
            productService.save(product);
            updateSummary();
        });

        colThreshold.setCellValueFactory(new PropertyValueFactory<>("lowStockThreshold"));
        colThreshold.setCellFactory(
                TextFieldTableCell.forTableColumn(new javafx.util.converter.BigDecimalStringConverter()));
        colThreshold.setOnEditCommit(event -> {
            Product product = event.getRowValue();
            product.setLowStockThreshold(event.getNewValue());
            productService.save(product);
            updateSummary(); // Low stock count might change
        });

        colForecast.setCellValueFactory(data -> {
            BigDecimal days = aiService.predictDaysRemaining(data.getValue());
            if (days == null)
                return new javafx.beans.property.SimpleStringProperty("-");
            String text = days.compareTo(BigDecimal.valueOf(90)) > 0 ? "> 3 oy" : days.toString() + " kun";
            return new javafx.beans.property.SimpleStringProperty(text);
        });

        // Row highlighting for low stock
        inventoryTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    BigDecimal stock = item.getStockQuantity();
                    BigDecimal threshold = item.getLowStockThreshold();
                    if (stock != null && threshold != null && stock.compareTo(threshold) <= 0) {
                        setStyle("-fx-background-color: #ffe6e6;"); // Light red hint
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        colAction.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(5);
                    Button reorderBtn = new Button("Buyurtma");
                    reorderBtn.getStyleClass().add("button-secondary");
                    reorderBtn.setOnAction(e -> showAlert("Zaxirani buyurtma qilish", "Buyurtma so'rovi yaratildi."));

                    Button posBtn = new Button("Narx");
                    posBtn.getStyleClass().add("button-nav");
                    posBtn.setOnAction(e -> showPriceHistory(getTableView().getItems().get(getIndex())));

                    Button batchesBtn = new Button("Partiyalar");
                    batchesBtn.getStyleClass().add("button-primary");
                    batchesBtn.setOnAction(e -> showBatchesDialog(getTableView().getItems().get(getIndex())));

                    Button labelBtn = new Button("Stiker 🏷️");
                    labelBtn.getStyleClass().add("button-secondary");
                    labelBtn.setOnAction(e -> showLabelDialog(getTableView().getItems().get(getIndex())));

                    box.getChildren().addAll(reorderBtn, posBtn, batchesBtn, labelBtn);
                    setGraphic(box);
                }
            }
        });
    }

    private void showBatchesDialog(Product product) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Partiya va muddati - " + product.getName());
        alert.setHeaderText(product.getName() + " uchun zaxira partiyalari");

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-15s | %-10s | %-12s | %-12s\n", "Partiya #", "Soni", "Kelgan", "Muddati"));
        sb.append("------------------------------------------------------------\n");
        sb.append(String.format("%-15s | %-10.2f | %-12s | %-12s\n",
                "DEFAULT-001", product.getStockQuantity(), "2026-01-01", "2026-12-31"));

        TextArea textArea = new TextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(javafx.scene.text.Font.font("Monospaced", 12));
        textArea.setPrefHeight(250);
        textArea.setPrefWidth(500);

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    private void showPriceHistory(Product product) {
        List<com.smartpos.model.PriceHistory> history = priceHistoryRepository
                .findByProductOrderByChangeDateDesc(product);

        if (history.isEmpty()) {
            showAlert("Narxlar tarixi", product.getName() + " uchun narx o'zgarishlari topilmadi.");
            return;
        }

        StringBuilder sb = new StringBuilder("Price History for " + product.getName() + ":\n\n");
        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (com.smartpos.model.PriceHistory entry : history) {
            sb.append(String.format("[%s] By: %s | %.2f -> %.2f\n",
                    entry.getChangeDate().format(dtf),
                    entry.getChangedBy().getUsername(),
                    entry.getOldPrice(),
                    entry.getNewPrice()));
        }

        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Narxlar tarixi");
        dialog.setHeaderText(product.getName());
        dialog.setContentText(sb.toString());
        dialog.setResizable(true);
        dialog.getDialogPane().setPrefWidth(500);
        dialog.showAndWait();
    }

    private void showLabelDialog(Product product) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Shtrix-kod Stikeri - " + product.getName());
        alert.setHeaderText("Termal printer uchun ZPL kod yoki HTML ko'rinishi");

        String zpl = barcodeService.generateZPL(product);

        TextArea textArea = new TextArea(zpl);
        textArea.setEditable(false);
        textArea.setFont(javafx.scene.text.Font.font("Monospaced", 11));

        VBox content = new VBox(10, new Label("ZPL (Zebra) Printer Kodu:"), textArea);

        Button printBtn = new Button("Printerga yuborish 🖨️");
        printBtn.getStyleClass().add("button-primary");
        printBtn.setMaxWidth(Double.MAX_VALUE);
        printBtn.setOnAction(e -> {
            System.out.println("🖨️ Printing ZPL to Raw LP Port...\n" + zpl);
            printBtn.setText("Yuborildi ✅");
            printBtn.setDisable(true);
        });

        content.getChildren().add(printBtn);
        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }

    private void loadInventory() {
        List<Product> products = productService.findAll();
        inventoryTable.setItems(FXCollections.observableArrayList(products));
        updateSummary();
    }

    private void updateSummary() {
        List<Product> items = inventoryTable.getItems();
        totalItemsLabel.setText(String.valueOf(items.size()));

        long lowStock = items.stream()
                .filter(p -> {
                    BigDecimal stock = p.getStockQuantity();
                    BigDecimal threshold = p.getLowStockThreshold();
                    return stock != null && threshold != null && stock.compareTo(threshold) <= 0;
                })
                .count();
        lowStockLabel.setText(String.valueOf(lowStock));
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.show();
    }
}
