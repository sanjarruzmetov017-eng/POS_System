package com.smartpos.service;

import com.smartpos.model.Sale;
import com.smartpos.repository.SaleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SyncService {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private ConnectivityService connectivityService;

    @Scheduled(fixedRate = 60000) // Attempt sync every 1 minute
    public void syncData() {
        if (!connectivityService.isOnline()) {
            return;
        }

        List<Sale> unsyncedSales = saleRepository.findBySyncedFalse();
        if (unsyncedSales.isEmpty()) {
            return;
        }

        System.out.println("🔄 Syncing " + unsyncedSales.size() + " records to cloud...");

        for (Sale sale : unsyncedSales) {
            try {
                // Mock cloud push logic
                simulateCloudPush(sale);

                sale.setSynced(true);
                saleRepository.save(sale);
            } catch (Exception e) {
                System.err.println("❌ Failed to sync sale ID: " + sale.getId());
            }
        }
    }

    private void simulateCloudPush(Sale sale) throws Exception {
        // In a real app, this would be a RestTemplate/WebClient call to a cloud API
        Thread.sleep(100); // Simulate network delay
    }
}
