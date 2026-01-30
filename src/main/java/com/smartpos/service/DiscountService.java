package com.smartpos.service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class DiscountService {

    public BigDecimal calculateDiscount(BigDecimal total, String promoCode) {
        if (promoCode == null)
            return BigDecimal.ZERO;

        switch (promoCode.toUpperCase()) {
            case "WELCOME10":
                return total.multiply(new BigDecimal("0.10"));
            case "BIGSALE20":
                return total.multiply(new BigDecimal("0.20"));
            case "FIXED50":
                return new BigDecimal("50.00");
            default:
                return BigDecimal.ZERO;
        }
    }
}
