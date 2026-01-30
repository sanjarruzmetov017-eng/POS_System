package com.smartpos.ui;

import com.smartpos.model.Expense;
import com.smartpos.service.ExpenseService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Component
public class ExpenseViewController {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private com.smartpos.service.UserService userService;

    @FXML
    private TableView<Expense> expenseTable;
    @FXML
    private TableColumn<Expense, Long> colId;
    @FXML
    private TableColumn<Expense, String> colDesc;
    @FXML
    private TableColumn<Expense, String> colCategory;
    @FXML
    private TableColumn<Expense, BigDecimal> colAmount;
    @FXML
    private TableColumn<Expense, String> colDate;
    @FXML
    private TableColumn<Expense, Void> colAction;

    @FXML
    private Label totalExpensesLabel;

    @FXML
    public void initialize() {
        setupTable();
        loadExpenses();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));

        colDate.setCellValueFactory(data -> {
            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            return new javafx.beans.property.SimpleStringProperty(data.getValue().getDate().format(dtf));
        });

        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Delete");
            {
                btn.getStyleClass().add("nav-button-critical");
                btn.setOnAction(e -> {
                    Expense exp = getTableView().getItems().get(getIndex());
                    expenseService.delete(exp.getId());
                    loadExpenses();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    private void loadExpenses() {
        List<Expense> expenses = expenseService.findAll();
        expenseTable.setItems(FXCollections.observableArrayList(expenses));

        BigDecimal total = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalExpensesLabel.setText("$" + String.format("%.2f", total));
    }

    @FXML
    public void handleAddExpense() {
        // Simple manual input dialog for now
        TextInputDialog descDialog = new TextInputDialog();
        descDialog.setTitle("Add Expense");
        descDialog.setHeaderText("Enter expense details");
        descDialog.setContentText("Description:");

        Optional<String> desc = descDialog.showAndWait();
        if (desc.isPresent()) {
            TextInputDialog amountDialog = new TextInputDialog("0.00");
            amountDialog.setTitle("Add Expense");
            amountDialog.setHeaderText("Amount for " + desc.get());
            amountDialog.setContentText("Amount:");

            Optional<String> amountStr = amountDialog.showAndWait();
            if (amountStr.isPresent()) {
                try {
                    Expense expense = new Expense();
                    expense.setDescription(desc.get());
                    expense.setAmount(new BigDecimal(amountStr.get()));
                    expense.setCategory("OTHER");
                    expense.setRecordedBy(userService.findAll().get(0)); // Placeholder
                    expenseService.save(expense);
                    loadExpenses();
                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Invalid amount.");
                    alert.show();
                }
            }
        }
    }
}
