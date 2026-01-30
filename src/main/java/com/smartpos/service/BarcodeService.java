package com.smartpos.service;

import com.smartpos.model.Product;
import org.springframework.stereotype.Service;

@Service
public class BarcodeService {

    /**
     * Generates a simple ZPL (Zebra Programming Language) string for thermal
     * printers.
     * ZPL is standard for most industrial label printers.
     */
    public String generateZPL(Product product) {
        return String.format(
                "^XA\n" +
                        "^FO50,50^A0N,36,36^FD%s^FS\n" + // Product Name
                        "^FO50,100^B3N,N,60,Y,N^FD%s^FS\n" + // Barcode (Code 39)
                        "^FO50,200^A0N,24,24^FDNarxi: %.2f so'm^FS\n" + // Price
                        "^XZ",
                product.getName(),
                product.getBarcode() != null ? product.getBarcode() : "NOBARCODE",
                product.getPrice().doubleValue());
    }

    /**
     * Mock SVG barcode generation for screen display.
     */
    public String generateHtmlLabel(Product product) {
        return String.format(
                "<div style='border: 1px solid #ccc; padding: 10px; width: 200px; font-family: sans-serif;'>" +
                        "<h3>%s</h3>" +
                        "<div style='background: repeating-linear-gradient(90deg, #000, #000 2px, #fff 2px, #fff 4px); height: 50px;'></div>"
                        +
                        "<p style='text-align: center;'>%s</p>" +
                        "<p style='font-weight: bold;'>Narxi: %.2f so'm</p>" +
                        "</div>",
                product.getName(),
                product.getBarcode(),
                product.getPrice().doubleValue());
    }
}
