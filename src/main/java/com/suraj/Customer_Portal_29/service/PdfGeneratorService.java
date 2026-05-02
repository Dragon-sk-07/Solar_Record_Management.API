package com.suraj.Customer_Portal_29.service;

import com.itextpdf.html2pdf.HtmlConverter;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class PdfGeneratorService {

    private final TemplateEngine templateEngine;

    public PdfGeneratorService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    // Existing generatePdf method
    public byte[] generatePdf(String type, Map<String, Object> data) {
        try {
            Context context = new Context();
            context.setVariables(data);

            String templateName = mapTemplateName(type);
            String htmlContent = templateEngine.process("pdf/" + templateName, context);

            String fullHtml = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "    <meta charset='UTF-8'>\n" +
                    "    <style>\n" +
                    "        body { font-family: Arial, sans-serif; margin: 20px; }\n" +
                    "        table { border-collapse: collapse; width: 100%; margin-bottom: 10px; }\n" +
                    "        td, th { border: 1px solid black; padding: 6px; vertical-align: top; }\n" +
                    "        h1 { font-size: 16px; text-align: center; }\n" +
                    "        h2 { font-size: 14px; text-align: center; }\n" +
                    "        .page-break { page-break-before: always; }\n" +
                    "    </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    htmlContent + "\n" +
                    "</body>\n" +
                    "</html>";

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            HtmlConverter.convertToPdf(fullHtml, outputStream);

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed for type: " + type + " - " + e.getMessage(), e);
        }
    }

    // ADD THIS - Async version
    public CompletableFuture<byte[]> generatePdfAsync(String type, Map<String, Object> data) {
        return CompletableFuture.supplyAsync(() -> generatePdf(type, data));
    }

    // ADD THIS - Static method for image to base64 conversion
    public static String imageToBase64(byte[] imageBytes, String mimeType) {
        return java.util.Base64.getEncoder().encodeToString(imageBytes);
    }

    private String mapTemplateName(String type) {
        switch(type) {
            case "wcr": return "WCR_Undertaking_Guarantee";
            case "proforma-a": return "Annexure-I_Proforma-A";
            case "dcr": return "Declaration_FOR_DCR";
            case "agreement": return "NET_METERING_CONNECTION_AGREEMENT";
            case "site-photos": return "Site-photos";
            default: return type;
        }
    }
}