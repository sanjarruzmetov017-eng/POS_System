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
         * Generates a REAL scanable barcode image (Base64) using ZXing.
         */
        public String generateHtmlLabel(Product product) {
                String barcodeText = product.getBarcode() != null && !product.getBarcode().isEmpty()
                                ? product.getBarcode()
                                : "00000000"; // Fallback

                String base64Image = "";
                try {
                        com.google.zxing.Writer writer = new com.google.zxing.MultiFormatWriter();
                        com.google.zxing.common.BitMatrix bitMatrix = writer.encode(
                                        barcodeText,
                                        com.google.zxing.BarcodeFormat.CODE_128,
                                        300, 100);

                        java.io.ByteArrayOutputStream pngOutputStream = new java.io.ByteArrayOutputStream();
                        com.google.zxing.client.j2se.MatrixToImageWriter.writeToStream(bitMatrix, "PNG",
                                        pngOutputStream);
                        byte[] pngData = pngOutputStream.toByteArray();
                        base64Image = java.util.Base64.getEncoder().encodeToString(pngData);
                } catch (Exception e) {
                        e.printStackTrace();
                        base64Image = ""; // Handle error gracefully
                }

                return String.format(
                                "<div style='border: 1px solid #ccc; padding: 15px; width: 250px; font-family: sans-serif; text-align: center; border-radius: 8px; background: white;'>"
                                                +
                                                "<h3 style='margin: 0 0 10px 0; color: #333;'>%s</h3>" +
                                                "<img src='data:image/png;base64,%s' style='width: 100%%; height: auto;' alt='%s'/>"
                                                +
                                                "<p style='margin: 5px 0 0 0; letter-spacing: 2px; font-family: monospace;'>%s</p>"
                                                +
                                                "<h4 style='margin: 10px 0 0 0; color: #007bff;'>%.2f so'm</h4>" +
                                                "</div>",
                                product.getName(),
                                base64Image,
                                barcodeText,
                                barcodeText,
                                product.getPrice().doubleValue());
        }
}
