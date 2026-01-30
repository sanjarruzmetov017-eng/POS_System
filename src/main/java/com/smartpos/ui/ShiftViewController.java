package com.smartpos.ui;

import com.smartpos.model.CashSession;
import com.smartpos.repository.CashSessionRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ShiftViewController {

    @Autowired
    private CashSessionRepository cashSessionRepository;

    @FXML
    private TableView<CashSession> shiftTable;
    @FXML
    private TableColumn<CashSession, Long> colId;
    @FXML
    private TableColumn<CashSession, String> colUser;
    @FXML
    private TableColumn<CashSession, String> colStart;
    @FXML
    private TableColumn<CashSession, String> colEnd;
    @FXML
    private TableColumn<CashSession, BigDecimal> colStartCash;
    @FXML
    private TableColumn<CashSession, BigDecimal> colActualCash;
    @FXML
    private TableColumn<CashSession, BigDecimal> colDiscrepancy;
    @FXML
    private TableColumn<CashSession, String> colStatus;

    @FXML
    public void initialize() {
        setupTable();
        loadShifts();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUser.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getUser().getUsername()));

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        colStart.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStartTime().format(dtf)));

        colEnd.setCellValueFactory(data -> {
            if (data.getValue().getEndTime() == null)
                return new javafx.beans.property.SimpleStringProperty("-");
            return new javafx.beans.property.SimpleStringProperty(data.getValue().getEndTime().format(dtf));
        });

        colStartCash.setCellValueFactory(new PropertyValueFactory<>("startCash"));
        colActualCash.setCellValueFactory(new PropertyValueFactory<>("endCash"));
        colDiscrepancy.setCellValueFactory(new PropertyValueFactory<>("discrepancy"));

        colStatus.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(data.getValue().isOpen() ? "OPEN" : "CLOSED"));

        // Color row if discrepancy != 0
        shiftTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(CashSession item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (item.getDiscrepancy() != null && item.getDiscrepancy().compareTo(BigDecimal.ZERO) != 0) {
                    setStyle("-fx-background-color: #fff0f0;"); // Hint of red for discrepancy
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void loadShifts() {
        List<CashSession> shifts = cashSessionRepository.findAll();
        // Sort by ID descending (newest first)
        shifts.sort((s1, s2) -> s2.getId().compareTo(s1.getId()));
        shiftTable.setItems(FXCollections.observableArrayList(shifts));
    }
}
