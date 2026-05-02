package com.suraj.Customer_Portal_29.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class WordGeneratorService {

    private final TemplateEngine templateEngine;

    public WordGeneratorService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    // ADD THIS MISSING METHOD
    public byte[] generateWord(String templateName, Map<String, Object> data) {
        try {
            Context context = new Context();
            context.setVariables(data);

            String actualTemplate = mapTemplateName(templateName);
            String htmlContent = templateEngine.process("pdf/" + actualTemplate, context);

            Document doc = Jsoup.parse(htmlContent);
            String finalHtml = doc.body().html();
            String wordHtml = buildWordWrapper(finalHtml);

            return wordHtml.getBytes(StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("Word generation failed: " + e.getMessage(), e);
        }
    }

    private String mapTemplateName(String templateName) {
        switch(templateName) {
            case "wcr": return "WCR_Undertaking_Guarantee";
            case "proforma-a": return "Annexure-I_Proforma-A";
            case "dcr": return "Declaration_FOR_DCR";
            case "agreement": return "NET_METERING_CONNECTION_AGREEMENT";
            case "site-photos": return "Site-photos";
            default: return templateName;
        }
    }

    private String buildWordWrapper(String content) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head>" +
                "<body style='font-family:Arial; margin:1cm;'>" + content + "</body></html>";
    }

    public byte[] generateCombinedWord(Map<String, Object> data) {
        try {
            String wcr = processTemplate("WCR_Undertaking_Guarantee", data);
            String annexure = processTemplate("Annexure-I_Proforma-A", data);
            String dcr = processTemplate("Declaration_FOR_DCR", data);
            String agreement = processTemplate("NET_METERING_CONNECTION_AGREEMENT", data);
            String photos = processTemplate("Site-photos", data);

            String finalHtml = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "    <meta charset='UTF-8'>\n" +
                    "    <title>Solar Record Document</title>\n" +
                    "    <style>\n" +
                    "        body { font-family: Arial, sans-serif; margin: 1cm; }\n" +
                    "        .page-break { page-break-before: always; }\n" +
                    "        table { border-collapse: collapse; width: 100%; margin-bottom: 10px; }\n" +
                    "        td, th { border: 1px solid black; padding: 6px; vertical-align: top; }\n" +
                    "        h1 { font-size: 16px; text-align: center; }\n" +
                    "        h2 { font-size: 14px; text-align: center; }\n" +
                    "        img { max-width: 100%; height: auto; }\n" +
                    "    </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    wcr + "\n" +
                    "<div class='page-break'></div>\n" +
                    annexure + "\n" +
                    "<div class='page-break'></div>\n" +
                    dcr + "\n" +
                    "<div class='page-break'></div>\n" +
                    agreement + "\n" +
                    "<div class='page-break'></div>\n" +
                    photos + "\n" +
                    "</body>\n" +
                    "</html>";

            return finalHtml.getBytes(StandardCharsets.UTF_8);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to generate Word file: " + e.getMessage(), e);
        }
    }

    private String processTemplate(String templateName, Map<String, Object> data) {
        try {
            Context context = new Context();
            context.setVariables(data);
            String html = templateEngine.process("pdf/" + templateName, context);
            Document doc = Jsoup.parse(html);

            Elements scripts = doc.select("script");
            scripts.remove();

            return doc.body().html();

        } catch (Exception e) {
            System.err.println("Error processing: " + templateName + " - " + e.getMessage());
            return "<div>Error loading " + templateName + "</div>";
        }
    }
}