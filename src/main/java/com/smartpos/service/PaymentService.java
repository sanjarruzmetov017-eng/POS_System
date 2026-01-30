package com.smartpos.service;

import com.smartpos.model.Sale;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class PaymentService {

    public enum PaymentProvider {
        CLICK("Click"),
        PAYME("Payme"),
        UZUM("Uzum Pay");

        private final String displayName;

        PaymentProvider(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public String generateQrPayload(Sale sale, PaymentProvider provider) {
        // Mocking a real payment URL/Deep link
        // In real life, this would be:
        // https://m.click.uz/service/pay?service_id=...&amount=...
        return String.format("https://smartpos.uz/pay/%s?sale_id=%s&amount=%s",
                provider.name().toLowerCase(),
                sale.getId() != null ? sale.getId() : "NEW",
                sale.getTotalAmount());
    }

    public boolean processDigitalPayment(Sale sale) {
        // Simulating an API callback or polling for payment status
        System.out.println("🔄 Processing digital payment for Sale ID: " + sale.getId());
        return true; // Mock: Payment always successful
    }
}
