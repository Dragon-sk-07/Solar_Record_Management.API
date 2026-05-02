package com.suraj.Customer_Portal_29.service;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.springframework.scheduling.annotation.Async;
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

    public byte[] generatePdf(String templateName, Map<String, Object> data) {

        try {

            Context context = new Context();
            context.setVariables(data);

            // Generate HTML from Thymeleaf
            String html = templateEngine.process("pdf/" + templateName, context);

            // Remove BOM if exists
            html = html.replace("\uFEFF", "");

            // Convert HTML → valid XHTML using Jsoup
            Document document = Jsoup.parse(html);

            document.outputSettings()
                    .syntax(Document.OutputSettings.Syntax.xml)
                    .escapeMode(Entities.EscapeMode.xhtml)
                    .charset("UTF-8");

            String xhtml = document.html();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(xhtml, null);
            builder.toStream(outputStream);
            builder.run();

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }
    public static String imageToBase64(byte[] imageBytes, String mimeType) {
        String base64 = java.util.Base64.getEncoder().encodeToString(imageBytes);
        return "data:" + mimeType + ";base64," + base64;
    }
    @Async
    public CompletableFuture<byte[]> generatePdfAsync(String type, Map<String, Object> data) {
        return CompletableFuture.completedFuture(generatePdf(type, data));
    }
}