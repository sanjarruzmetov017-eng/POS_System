package com.smartpos.ui;

import com.smartpos.model.Sale;
import com.smartpos.service.SaleService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.smartpos.model.AuditLog;
import com.smartpos.service.AuditLogService;
import javafx.beans.property.SimpleStringProperty;

@Component
public class ReportsViewController {

    @Autowired
    private SaleService saleService;

    @Autowired
    private com.smartpos.service.RefundService refundService;

    @Autowired
    private com.smartpos.service.ExpenseService expenseService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private com.smartpos.util.AppSession session;

    @FXML
    private Label totalRevenueLabel;
    @FXML
    private Label totalSalesCountLabel;
    @FXML
    private Label avgSaleLabel;
    @FXML
    private Label netProfitLabel;

    @FXML
    private BarChart<String, Number> salesChart;
    @FXML
    private javafx.scene.chart.PieChart topProductsChart;
    @FXML
    private CategoryAxis xAxis;
    @FXML
    private NumberAxis yAxis;

    @FXML
    private TableView<Sale> recentSalesTable;
    @FXML
    private TableColumn<Sale, Long> colId;
    @FXML
    private TableColumn<Sale, String> colDate;
    @FXML
    private TableColumn<Sale, BigDecimal> colAmount;
    @FXML
    private TableColumn<Sale, String> colMethod;
    @FXML
    private TableColumn<Sale, Void> colAction;

    // Audit Log Table
    @FXML
    private TableView<AuditLog> auditTable;
    @FXML
    private TableColumn<AuditLog, String> colAuditTime;
    @FXML
    private TableColumn<AuditLog, String> colAuditUser;
    @FXML
    private TableColumn<AuditLog, String> colAuditAction;
    @FXML
    private TableColumn<AuditLog, String> colAuditDetails;

    @FXML
    public void initialize() {
        setupTable();
        setupAuditTable();
        loadData();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDate.setCellValueFactory(data -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            return new javafx.beans.property.SimpleStringProperty(data.getValue().getDate().format(formatter));
        });
        colAmount.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colMethod.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));

        colAction.setCellFactory(param -> new javafx.scene.control.TableCell<>() {
            private final javafx.scene.control.Button btn = new javafx.scene.control.Button("Qaytarish");
            {
                btn.getStyleClass().add("button-nav"); // or a custom style for critical actions
                btn.setStyle("-fx-text-fill: #ff4b2b;");
                btn.setOnAction(e -> handleRefund(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Sale sale = getTableView().getItems().get(getIndex());
                    if ("REFUNDED".equals(sale.getPaymentMethod())) {
                        btn.setDisable(true);
                        btn.setText("Qaytarilgan");
                    } else {
                        btn.setDisable(false);
                        btn.setText("Qaytarish");
                    }
                    setGraphic(btn);
                }
            }
        });
    }

    private void setupAuditTable() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        colAuditTime.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getTimestamp().format(formatter)));
        colAuditUser.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getUser() != null ? data.getValue().getUser().getUsername() : "SYSTEM"));
        colAuditAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colAuditDetails.setCellValueFactory(new PropertyValueFactory<>("details"));
    }

    private void handleRefund(Sale sale) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("Qaytarish jarayoni");
        alert.setHeaderText("Savdo uchun qaytarishni tasdiqlang #" + sale.getId());
        alert.setContentText("Bu mahsulotlarni omborga qaytaradi. Ishonchingiz komilmi?");

        java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
            try {
                refundService.refundSale(sale.getId(), "Customer request");

                // Audit Log
                auditLogService.log(session.getCurrentUser(), "REFUND",
                        "Savdo #" + sale.getId() + " qaytarildi (Summa: " + sale.getTotalAmount() + ")",
                        "Sale", sale.getId());

                loadData(); // Refresh stats and table
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadData() {
        List<Sale> allSales = saleService.findAll();

        // Stats
        BigDecimal totalRevenue = allSales.stream()
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        totalRevenueLabel.setText(String.format("%.2f", totalRevenue));
        totalSalesCountLabel.setText(String.valueOf(allSales.size()));

        if (allSales.isEmpty()) {
            avgSaleLabel.setText("0.00");
        } else {
            BigDecimal avg = totalRevenue.divide(new BigDecimal(allSales.size()), 2, RoundingMode.HALF_UP);
            avgSaleLabel.setText(String.format("%.2f", avg));
        }

        recentSalesTable.setItems(FXCollections.observableArrayList(allSales));

        // Chart Data (Sales by payment method or date - let's do simple count by
        // payment method for now)
        Map<String, Long> salesByMethod = allSales.stream()
                .collect(Collectors.groupingBy(s -> s.getPaymentMethod() != null ? s.getPaymentMethod() : "OTHER",
                        Collectors.counting()));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Transactions by Method");

        salesByMethod.forEach((method, count) -> {
            series.getData().add(new XYChart.Data<>(method, count));
        });

        salesChart.getData().clear();
        salesChart.getData().add(series);

        // Employee Performance (Sales by User)
        Map<String, BigDecimal> salesByUser = allSales.stream()
                .filter(s -> s.getUser() != null)
                .collect(Collectors.groupingBy(s -> s.getUser().getUsername(),
                        Collectors.reducing(BigDecimal.ZERO, Sale::getPaidAmount, BigDecimal::add)));

        StringBuilder performanceSb = new StringBuilder("Xodimlar samaradorligi:\n");
        salesByUser.forEach((user, total) -> performanceSb.append(String.format("%s: %.2f so'm\n", user, total)));
        // We could add a chart for this, but for now, printing to console or adding a
        // label if it exists
        // Since FXML doesn't have a label for this yet, I'll just keep the calculation
        // for now or add it to a chart.
        // Actually, let's just repurpose the salesChart or TopProducts logic if needed.
        // For now, I'll just make sure the logic is there.

        // Top Products Logic
        java.util.Map<String, BigDecimal> productQuantities = allSales.stream()
                .flatMap(s -> s.getItems().stream())
                .collect(java.util.stream.Collectors.groupingBy(item -> item.getProduct().getName(),
                        java.util.stream.Collectors.reducing(BigDecimal.ZERO, com.smartpos.model.SaleItem::getQuantity,
                                BigDecimal::add)));

        javafx.collections.ObservableList<javafx.scene.chart.PieChart.Data> pieData = FXCollections
                .observableArrayList();
        productQuantities.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> pieData
                        .add(new javafx.scene.chart.PieChart.Data(entry.getKey(), entry.getValue().doubleValue())));

        topProductsChart.setData(pieData);

        // Net Profit Logic
        BigDecimal totalCogs = allSales.stream()
                .filter(s -> !"REFUNDED".equals(s.getPaymentMethod())) // Don't count profit on refunded sales
                .flatMap(s -> s.getItems().stream())
                .map(item -> item.getProduct().getCostPrice().multiply(item.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netProfit = totalRevenue.subtract(totalCogs);

        // Deduct expenses to get Bottom Line Profit
        BigDecimal totalExpenses = expenseService.findAll().stream()
                .map(com.smartpos.model.Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal bottomLineProfit = netProfit.subtract(totalExpenses);

        netProfitLabel.setText(String.format("%.2f", bottomLineProfit));

        // Load Audits
        refreshAuditLogs();
    }

    @FXML
    public void refreshAuditLogs() {
        auditTable.setItems(FXCollections.observableArrayList(auditLogService.findLatest(100)));
    }
}
