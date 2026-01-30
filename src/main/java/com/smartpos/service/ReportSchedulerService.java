package com.smartpos.service;

import com.smartpos.model.Sale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReportSchedulerService {

        @Autowired
        private SaleService saleService;

        @Autowired
        private TelegramService telegramService;

        // Run every day at 11:59 PM (Mock: every minute for demonstration if needed,
        // but keeping it daily)
        @Scheduled(cron = "0 59 23 * * *")
        public void sendDailyTelegramReport() {
                LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0);
                List<Sale> todaySales = saleService.findAll().stream()
                                .filter(s -> s.getDate().isAfter(todayStart))
                                .toList();

                BigDecimal revenue = todaySales.stream()
                                .map(Sale::getPaidAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                String summary = String.format("""
                                📊 *SmartPOS Kunlik Xulosasi*
                                📅 Sana: %s
                                ------------------------
                                💰 Jami Savdo: %s so'm
                                🛒 Savdolar Soni: %d
                                ✅ Holati: Smena muvaffaqiyatli yopildi
                                """, LocalDateTime.now().toLocalDate().toString(), revenue, todaySales.size());

                telegramService.sendDailySummary(summary);
        }
}
