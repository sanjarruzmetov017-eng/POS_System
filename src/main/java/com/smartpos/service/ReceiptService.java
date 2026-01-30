package com.smartpos.service;

import com.smartpos.model.Sale;
import com.smartpos.ui.SalesViewController.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReceiptService {

    @Autowired
    private SystemConfigService configService;

    public String generateReceipt(Sale sale, List<CartItem> cartItems) {
        StringBuilder sb = new StringBuilder();
        String line = "--------------------------------\n";

        String storeName = configService.getConfig("STORE_NAME", "SMART POS TIZIMI");
        String storePhone = configService.getConfig("STORE_PHONE", "");

        sb.append(String.format("%32s\n", storeName));
        if (!storePhone.isEmpty()) {
            sb.append(String.format("%32s\n", "Tel: " + storePhone));
        }
        sb.append(line);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        sb.append("Sana: ").append(sale.getDate().format(formatter)).append("\n");
        sb.append("Savdo ID: ").append(sale.getId() != null ? sale.getId() : "YANGI").append("\n");
        if (sale.getCustomer() != null) {
            sb.append("Mijoz: ").append(sale.getCustomer().getName()).append("\n");
        }
        sb.append(line);

        sb.append(String.format("%-18s %3s %8s\n", "Mahsulot", "Son", "Jami"));
        sb.append(line);

        for (CartItem item : cartItems) {
            String name = item.getProduct().getName();
            if (name.length() > 18) {
                name = name.substring(0, 15) + "...";
            }
            sb.append(String.format("%-18s %3d %8.2f\n",
                    name,
                    item.getQuantity().toBigInteger(),
                    item.getTotal().doubleValue()));
        }

        sb.append(line);
        sb.append(String.format("%-20s %10.2f so'm\n", "ORALIQ JAMI:",
                sale.getTotalAmount().add(sale.getDiscountAmount()).doubleValue()));
        if (sale.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("%-20s %10.2f so'm\n", "CHEGIRMA:", -sale.getDiscountAmount().doubleValue()));
        }
        sb.append(String.format("%-20s %10.2f so'm\n", "JAMI:", sale.getTotalAmount().doubleValue()));
        sb.append(line);
        sb.append(String.format("%-20s %10.2f so'm\n", "TO'LANDI:", sale.getPaidAmount().doubleValue()));
        if (sale.getBalanceDue().compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("%-20s %10.2f so'm\n", "QARZ:", sale.getBalanceDue().doubleValue()));
        }
        sb.append(line);
        sb.append(" To'lov turi: ").append(translatePaymentMethod(sale.getPaymentMethod())).append("\n");
        if (sale.getCustomer() != null) {
            sb.append(" Ballar: ").append(sale.getCustomer().getLoyaltyPoints()).append("\n");
        }
        sb.append(line);
        String footer = configService.getConfig("RECEIPT_FOOTER", "XARIDINGIZ UCHUN RAHMAT!");
        sb.append(String.format("%32s\n", footer));

        return sb.toString();
    }

    private String translatePaymentMethod(String method) {
        if ("CASH".equalsIgnoreCase(method))
            return "NAQD";
        if ("CARD".equalsIgnoreCase(method))
            return "KARTA";
        if ("DEBT".equalsIgnoreCase(method))
            return "QARZ";
        return method;
    }
}
