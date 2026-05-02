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

    private String mapTemplateName(String type) {
        switch(type) {
            case "wcr": return "WCR_Undertaking_Guarantee_Aadhar";
            case "proforma-a": return "Annexure-I_Proforma-A";
            case "dcr": return "Declaration_FOR_DCR";
            case "agreement": return "NET_METERING_CONNECTION_AGREEMENT";
            case "site-photos": return "Site-photos";
            default: return type;
        }
    }

    public byte[] generateWord(String type, Map<String, Object> data) {
        try {
            Context context = new Context();
            context.setVariables(data);
            String templateName = mapTemplateName(type);
            String htmlContent = templateEngine.process("pdf/" + templateName, context);
            Document doc = Jsoup.parse(htmlContent);
            fixImagesForWord(doc);
            String finalHtml = doc.body().html();
            String wordHtml = buildWordWrapper(finalHtml);
            return wordHtml.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Word generation failed: " + e.getMessage(), e);
        }
    }

    private void fixImagesForWord(Document doc) {
        Elements imgs = doc.select("img");

        for (Element img : imgs) {
            String src = img.attr("src");

            if (src != null && src.startsWith("data:image")) {
                img.attr("width", "500");
                img.attr("height", "auto");
                img.attr("border", "0");
                img.attr("style", "max-width:500px; height:auto; display:block; margin:0 auto;");
            }
        }

        Elements aadharBlocks = doc.select(".aadhar-block, .aadhar-image-box");
        for (Element block : aadharBlocks) {
            block.attr("style", "margin:15px 0; padding:15px; border:2px solid #000; text-align:center; background:#f9f9f9;");
        }

        Elements aadharText = doc.select(".aadhar-number-text");
        for (Element text : aadharText) {
            text.attr("style", "font-weight:bold; margin:10px 0; font-size:14px;");
        }
    }

    private String buildWordWrapper(String content) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset='UTF-8'>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; margin: 1.5cm; }\n" +
                "        img { max-width: 100%; height: auto; }\n" +
                "        table { border-collapse: collapse; width: 100%; margin-bottom: 10px; }\n" +
                "        td, th { border: 1px solid black; padding: 8px; vertical-align: top; }\n" +
                "        h1 { font-size: 18px; text-align: center; margin: 10px 0; }\n" +
                "        h2 { font-size: 16px; text-align: center; margin: 8px 0; }\n" +
                "        .page-break { page-break-before: always; }\n" +
                "        .aadhar-block { margin: 20px 0; padding: 15px; border: 2px solid #000; text-align: center; background: #f9f9f9; }\n" +
                "        .aadhar-image-box { text-align: center; margin: 15px 0; }\n" +
                "        .aadhar-number-text { font-weight: bold; margin: 10px 0; font-size: 14px; }\n" +
                "        .signature-row { margin-top: 30px; }\n" +
                "        .signature-left, .signature-right { width: 50%; float: left; text-align: center; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                content +
                "</body>\n" +
                "</html>";
    }

    public byte[] generateCombinedWord(Map<String, Object> data) {
        try {
            String wcr = processTemplate("WCR_Undertaking_Guarantee_Aadhar", data);
            String annexure = processTemplate("Annexure-I_Proforma-A", data);
            String dcr = processTemplate("Declaration_FOR_DCR", data);
            String agreement = processTemplate("NET_METERING_CONNECTION_AGREEMENT", data);
            String photos = processTemplate("Site-photos", data);

            String finalHtml = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head>\n" +
                    "    <meta charset='UTF-8'>\n" +
                    "    <style>\n" +
                    "        body { font-family: Arial, sans-serif; margin: 1.5cm; }\n" +
                    "        .page-break { page-break-before: always; }\n" +
                    "        table { border-collapse: collapse; width: 100%; margin-bottom: 10px; }\n" +
                    "        td, th { border: 1px solid black; padding: 8px; vertical-align: top; }\n" +
                    "        h1 { font-size: 18px; text-align: center; }\n" +
                    "        h2 { font-size: 16px; text-align: center; }\n" +
                    "        img { max-width: 100%; height: auto; }\n" +
                    "        .aadhar-block { margin: 20px 0; padding: 15px; border: 2px solid #000; text-align: center; background: #f9f9f9; }\n" +
                    "        .aadhar-image-box { text-align: center; margin: 15px 0; }\n" +
                    "        .aadhar-number-text { font-weight: bold; margin: 10px 0; font-size: 14px; }\n" +
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

            fixImagesForWord(doc);

            return doc.body().html();
        } catch (Exception e) {
            System.err.println("Error processing: " + templateName + " - " + e.getMessage());
            return "<div>Error loading " + templateName + "</div>";
        }
    }
}