package com.smartpos.service;

import com.smartpos.model.Sale;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;

@Service
public class TelegramService {

    // In a real scenario, these would come from settings/config
    private final String botToken = "MOCK_TOKEN";
    private final String channelId = "MOCK_CHANNEL_ID";

    public void sendReceipt(Sale sale, String receiptText) {
        // Mocking Telegram API call
        CompletableFuture.runAsync(() -> {
            try {
                System.out.println("📱 [TELEGRAM] Savdo uchun e-Chek yuborilmoqda #" + sale.getId());
                Thread.sleep(1000); // Simulate network latency
                System.out.println("✅ [TELEGRAM] Chek muvaffaqiyatli yuborildi.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    public void sendDailySummary(String summary) {
        CompletableFuture.runAsync(() -> {
            System.out.println("📊 [TELEGRAM] Kunlik boshqaruv xulosasi yuborilmoqda...");
            // Real integration would use RestTemplate/WebClient to POST to Telegram API
        });
    }
}
