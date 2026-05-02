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

            // Clean up for Word
            Elements scripts = doc.select("script");
            scripts.remove();

            return doc.body().html();

        } catch (Exception e) {
            System.err.println("Error processing: " + templateName + " - " + e.getMessage());
            return "<div>Error loading " + templateName + "</div>";
        }
    }
}