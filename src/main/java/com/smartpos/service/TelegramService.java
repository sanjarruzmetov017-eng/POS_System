package com.smartpos.service;

import com.smartpos.model.Sale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

@Service
public class TelegramService {

    @Autowired
    private SystemConfigService configService;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public void sendReceipt(Sale sale, String receiptText) {
        CompletableFuture.runAsync(() -> {
            String message = "🧾 *Yangi Savdo - #" + sale.getId() + "*\n\n" + receiptText;
            sendMessage(message);
        });
    }

    public void sendDailySummary(String summary) {
        CompletableFuture.runAsync(() -> {
            sendMessage("📊 *Kunlik Hisobot*\n\n" + summary);
        });
    }

    private void sendMessage(String text) {
        String token = configService.getConfig("TELEGRAM_BOT_TOKEN", "");
        String chatId = configService.getConfig("TELEGRAM_CHAT_ID", "");

        if (token.isEmpty() || token.startsWith("MOCK") || chatId.isEmpty()) {
            System.out.println("⚠️ [TELEGRAM MOCK] Token sozlanmagan. Xabar: "
                    + text.substring(0, Math.min(text.length(), 50)) + "...");
            return;
        }

        try {
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String url = "https://api.telegram.org/bot" + token + "/sendMessage?chat_id=" + chatId + "&text="
                    + encodedText + "&parse_mode=Markdown";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET() // Telegram allows GET for simple text
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("✅ [TELEGRAM] Xabar yuborildi.");
            } else {
                System.err.println("❌ [TELEGRAM ERROR] " + response.statusCode() + " - " + response.body());
                throw new RuntimeException("Telegram API Error: " + response.body());
            }

        } catch (Exception e) {
            System.err.println("❌ [TELEGRAM FAILURE] " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Tarmoq xatoligi: " + e.getMessage());
        }
    }
}
