package com.smartpos.ui;

import com.smartpos.model.Product;
import com.smartpos.model.Sale;
import com.smartpos.service.ProductService;
import com.smartpos.service.SaleService;
import com.smartpos.util.NotificationUtil;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import java.io.File;
import javafx.geometry.Pos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class SalesViewController {

    @Autowired
    private ProductService productService;
    @Autowired
    private SaleService saleService;
    @Autowired
    private NotificationUtil notificationUtil;
    @Autowired
    private com.smartpos.service.ReceiptService receiptService;
    @Autowired
    private com.smartpos.service.CustomerService customerService;
    @Autowired
    private com.smartpos.service.DiscountService discountService;
    @Autowired
    private com.smartpos.service.LoyaltyService loyaltyService;
    @Autowired
    private com.smartpos.service.DebtService debtService;
    @Autowired
    private com.smartpos.service.CashRegisterService cashRegisterService;
    @Autowired
    private com.smartpos.service.TelegramService telegramService;
    // Removed unused paymentService

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> paymentMethodComboBox;
    @FXML
    private TableView<Product> productTable;
    @FXML
    private TableColumn<Product, String> colProdImage;
    @FXML
    private TableColumn<Product, String> colProdName;
    @FXML
    private TableColumn<Product, BigDecimal> colProdPrice;
    @FXML
    private TableColumn<Product, Void> colProdAction;

    @FXML
    private TableView<CartItem> cartTable;
    @FXML
    private TableColumn<CartItem, String> colCartName;
    @FXML
    private TableColumn<CartItem, BigDecimal> colCartQty;
    @FXML
    private TableColumn<CartItem, BigDecimal> colCartTotal;

    @FXML
    private Label subtotalLabel;
    @FXML
    private Label discountLabel;
    @FXML
    private Label totalLabel;
    @FXML
    private Label balanceDueLabel;

    @FXML
    private ComboBox<com.smartpos.model.Customer> customerComboBox;
    @FXML
    private TextField promoField;
    @FXML
    private TextField paidAmountField;

    private ObservableList<CartItem> cartItems = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupProductTable();
        setupCartTable();
        loadProducts();

        searchField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal == null || newVal.isEmpty())
                loadProducts();
            else
                productTable.setItems(FXCollections.observableArrayList(productService.search(newVal)));
        });

        // Barcode scanning support
        searchField.setOnAction(e -> {
            String code = searchField.getText();
            if (code != null && !code.isEmpty()) {
                Optional<Product> product = productService.findByBarcode(code);
                if (product.isPresent()) {
                    addToCart(product.get());
                    searchField.clear();
                }
            }
        });

        // Load customers
        customerComboBox.setItems(FXCollections.observableArrayList(customerService.findAll()));
        customerComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(com.smartpos.model.Customer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getName());
            }
        });
        customerComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(com.smartpos.model.Customer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getName());
            }
        });

        // Load payment methods
        paymentMethodComboBox.setItems(FXCollections.observableArrayList("NAQD", "KARTA", "CLICK", "PAYME"));
        paymentMethodComboBox.getSelectionModel().selectFirst();

        // Listeners for real-time calculation
        promoField.textProperty().addListener((obs, old, newVal) -> updateTotal());
        paidAmountField.textProperty().addListener((obs, old, newVal) -> updateTotal());
    }

    private void setupProductTable() {
        colProdImage.setCellFactory(param -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitWidth(35);
                imageView.setFitHeight(35);
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

        colProdName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colProdPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colProdAction.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Qo'shish");
            {
                btn.getStyleClass().add("button-primary");
                btn.setOnAction(e -> addToCart(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    private void setupCartTable() {
        colCartName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProduct().getName()));
        colCartQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colCartTotal.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getTotal()));
        cartTable.setItems(cartItems);
    }

    private void loadProducts() {
        productTable.setItems(FXCollections.observableArrayList(productService.findAll()));
    }

    private void addToCart(Product product) {
        if (product == null)
            return;

        BigDecimal quantityToAdd = BigDecimal.ONE;

        if (product.isWeighted()) {
            TextInputDialog dialog = new TextInputDialog("1.0");
            dialog.setTitle("Tarozida o'lchash");
            dialog.setHeaderText(product.getName() + " uchun miqdorni kiriting (" + product.getUnitType() + ")");
            dialog.setContentText("Vazni:");
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                try {
                    quantityToAdd = new BigDecimal(result.get());
                } catch (NumberFormatException e) {
                    showAlert("Xato", "Noto'g'ri vazn kiritildi.");
                    return;
                }
            } else {
                return; // Cancelled
            }
        }

        // Check if exists
        for (CartItem item : cartItems) {
            if (item.getProduct().getId().equals(product.getId())) {
                BigDecimal newQty = item.getQuantity().add(quantityToAdd);
                if (newQty.compareTo(product.getStockQuantity()) > 0) {
                    showAlert("Omborda yetarli emas",
                            "Boshqa qo'shib bo'lmaydi. Faqat " + product.getStockQuantity() + " "
                                    + product.getUnitType() + " mavjud.");
                    return;
                }
                item.setQuantity(newQty);

                // Warn if near threshold
                if (product.getStockQuantity().subtract(newQty).compareTo(product.getLowStockThreshold()) <= 0) {
                    System.out.println("⚠️ Zaxira kamaymoqda: " + product.getName());
                }

                cartTable.refresh();
                updateTotal();
                return;
            }
        }

        if (product.getStockQuantity().compareTo(quantityToAdd) < 0) {
            showAlert("Omborda yetarli emas",
                    "Ushbu mahsulot hozirda mavjud emas (" + quantityToAdd + " dan kam mavjud).");
            return;
        }
        cartItems.add(new CartItem(product, quantityToAdd));
        updateTotal();
    }

    private void showAlert(String title, String content) {
        if ("Success".equalsIgnoreCase(title)) {
            notificationUtil.showSuccess(title, content);
        } else if ("Error".equalsIgnoreCase(title)) {
            notificationUtil.showError(title, content);
        } else {
            notificationUtil.showInfo(title, content);
        }
    }

    private void updateTotal() {
        BigDecimal subtotal = cartItems.stream()
                .map(CartItem::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = discountService.calculateDiscount(subtotal, promoField.getText());
        BigDecimal total = subtotal.subtract(discount);
        if (total.compareTo(BigDecimal.ZERO) < 0)
            total = BigDecimal.ZERO;

        BigDecimal paid = BigDecimal.ZERO;
        try {
            if (paidAmountField.getText() != null && !paidAmountField.getText().isEmpty()) {
                paid = new BigDecimal(paidAmountField.getText());
            }
        } catch (NumberFormatException e) {
            // Invalid input, ignore
        }

        BigDecimal balance = total.subtract(paid);
        if (balance.compareTo(BigDecimal.ZERO) < 0)
            balance = BigDecimal.ZERO;

        subtotalLabel.setText(String.format("%.2f", subtotal) + " so'm");
        discountLabel.setText("-" + String.format("%.2f", discount) + " so'm");
        totalLabel.setText(String.format("%.2f", total) + " so'm");
        balanceDueLabel.setText(String.format("%.2f", balance) + " so'm");
    }

    @FXML
    public void handleCheckout() {
        if (cartItems.isEmpty())
            return;

        BigDecimal subtotal = cartItems.stream().map(CartItem::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount = discountService.calculateDiscount(subtotal, promoField.getText());
        BigDecimal total = subtotal.subtract(discount);

        BigDecimal paid = BigDecimal.ZERO;
        try {
            if (paidAmountField.getText() != null && !paidAmountField.getText().isEmpty()) {
                paid = new BigDecimal(paidAmountField.getText());
            }
        } catch (NumberFormatException e) {
            showAlert("Xato", "Noto'g'ri to'lov miqdori.");
            return;
        }

        Sale sale = new Sale();
        sale.setPaidAmount(paid);
        sale.setBalanceDue(total.subtract(paid));
        sale.setCustomer(customerComboBox.getValue());

        String method = paymentMethodComboBox.getValue() != null ? paymentMethodComboBox.getValue() : "CASH";
        if (sale.getBalanceDue().compareTo(BigDecimal.ZERO) > 0) {
            method = "DEBT";
        }
        sale.setPaymentMethod(method);

        // Get active user from session
        cashRegisterService.getActiveSession().ifPresent(session -> sale.setUser(session.getUser()));

        // Save using service
        Sale savedSale = saleService.createSale(sale, new java.util.ArrayList<>(cartItems));

        // Process Debt & Loyalty
        if (sale.getCustomer() != null) {
            debtService.recordDebt(savedSale);
            loyaltyService.awardPoints(sale.getCustomer(), total);
        }

        // Generate Receipt
        String receiptText = receiptService.generateReceipt(savedSale, new java.util.ArrayList<>(cartItems));

        // Clear UI
        cartItems.clear();
        promoField.clear();
        paidAmountField.clear();
        customerComboBox.getSelectionModel().clearSelection();
        updateTotal();

        // Show Receipt Dialog
        showReceiptDialog(receiptText, savedSale);
    }

    private void showReceiptDialog(String receiptText, Sale sale) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Savdo yakunlandi - Chek");
        alert.setHeaderText(null);

        TextArea textArea = new TextArea(receiptText);
        textArea.setEditable(false);
        textArea.setFont(javafx.scene.text.Font.font("Monospaced", 12));
        textArea.setPrefHeight(400);
        textArea.setPrefWidth(300);

        VBox content = new VBox(10, textArea);
        Button telegramBtn = new Button("Telegramga yuborish 📱");
        telegramBtn.getStyleClass().add("button-primary");
        telegramBtn.setMaxWidth(Double.MAX_VALUE);
        telegramBtn.setOnAction(e -> {
            telegramService.sendReceipt(sale, receiptText);
            telegramBtn.setDisable(true);
            telegramBtn.setText("Yuborildi ✅");
        });

        content.getChildren().add(telegramBtn);

        // Manual Terminal Instruction
        if (!"NAQD".equals(sale.getPaymentMethod()) && !"DEBT".equals(sale.getPaymentMethod())) {
            Label manualNotice = new Label("TERMINALGA KIRITING: " + sale.getPaidAmount() + " so'm");
            manualNotice.setStyle(
                    "-fx-text-fill: #ff4444; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 10; -fx-border-color: #ff4444; -fx-border-width: 2; -fx-alignment: center;");
            manualNotice.setMaxWidth(Double.MAX_VALUE);
            content.getChildren().add(0, manualNotice);
        }

        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }

    public static class CartItem {
        private Product product;
        private BigDecimal quantity;

        public CartItem(Product product, BigDecimal quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        public Product getProduct() {
            return product;
        }

        public BigDecimal getQuantity() {
            return quantity;
        }

        public void setQuantity(BigDecimal q) {
            this.quantity = q;
        }

        public BigDecimal getTotal() {
            return product.getPrice().multiply(quantity);
        }
    }
}
