package com.smartpos.ui;

import com.smartpos.model.Tenant;
import com.smartpos.repository.TenantRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class SuperAdminController {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ApplicationContext applicationContext;

    @FXML
    private TableView<Tenant> tenantTable;
    @FXML
    private TextField searchField;
    @FXML
    private TableColumn<Tenant, Long> colId;
    @FXML
    private TableColumn<Tenant, String> colName;
    @FXML
    private TableColumn<Tenant, String> colOwner;
    @FXML
    private TableColumn<Tenant, String> colLicense;
    @FXML
    private TableColumn<Tenant, LocalDateTime> colExpiry;
    @FXML
    private TableColumn<Tenant, Void> colAction;

    @FXML
    public void initialize() {
        setupTable();
        loadTenants();

        searchField.textProperty().addListener((obs, old, newVal) -> {
            filterTenants(newVal);
        });
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colOwner.setCellValueFactory(new PropertyValueFactory<>("ownerName"));
        colLicense.setCellValueFactory(new PropertyValueFactory<>("licenseKey"));
        colExpiry.setCellValueFactory(new PropertyValueFactory<>("subscriptionExpiry"));

        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("O'chirish");
            {
                btn.getStyleClass().add("nav-button-critical");
                btn.setOnAction(event -> {
                    Tenant tenant = getTableView().getItems().get(getIndex());
                    tenantRepository.delete(tenant);
                    loadTenants();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty)
                    setGraphic(null);
                else
                    setGraphic(btn);
            }
        });
    }

    private void loadTenants() {
        List<Tenant> tenants = tenantRepository.findAll();
        tenantTable.setItems(FXCollections.observableArrayList(tenants));
    }

    private void filterTenants(String query) {
        if (query == null || query.trim().isEmpty()) {
            loadTenants();
            return;
        }
        List<Tenant> filtered = tenantRepository.findAll().stream()
                .filter(t -> t.getName().toLowerCase().contains(query.toLowerCase()) ||
                        (t.getOwnerName() != null && t.getOwnerName().toLowerCase().contains(query.toLowerCase())))
                .toList();
        tenantTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void handleNewTenant() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/tenant_dialog.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Yangi Do'kon Qo'shish");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadTenants();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
