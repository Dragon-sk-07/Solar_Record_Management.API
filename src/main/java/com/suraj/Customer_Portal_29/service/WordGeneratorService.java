package com.suraj.Customer_Portal_29.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
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
            case "indemnity": return "INDEMNITY_BOND";
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

            // Convert all images to Base64 with proper sizing
            convertAllImagesToBase64(doc);

            // Build final Word document
            String finalHtml = buildWordCompatibleWrapper(doc.html());
            return finalHtml.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Word generation failed: " + e.getMessage(), e);
        }
    }

    private void convertAllImagesToBase64(Document doc) {
        Elements imgs = doc.select("img");
        for (Element img : imgs) {
            String src = img.attr("src");
            String originalStyle = img.attr("style");

            if (src != null && !src.isEmpty()) {
                // Convert HTTP URLs to Base64
                if (src.startsWith("http")) {
                    try {
                        java.net.URL url = new java.net.URL(src);
                        byte[] imageBytes = url.openStream().readAllBytes();
                        String base64 = Base64.getEncoder().encodeToString(imageBytes);
                        String mimeType = src.contains(".png") ? "image/png" : "image/jpeg";
                        img.attr("src", "data:" + mimeType + ";base64," + base64);

                        // Preserve exact dimensions from original style
                        if (originalStyle.contains("max-height:60px") || originalStyle.contains("max-height:60")) {
                            img.attr("style", "height:60px; width:auto; object-fit:contain;");
                        } else if (originalStyle.contains("max-height:70px") || originalStyle.contains("max-height:70")) {
                            img.attr("style", "height:70px; width:auto; object-fit:contain;");
                        } else if (originalStyle.contains("max-height:50px") || originalStyle.contains("max-height:50")) {
                            img.attr("style", "height:50px; width:auto; object-fit:contain;");
                        } else if (originalStyle.contains("width:400px") || originalStyle.contains("width:400")) {
                            img.attr("style", "width:400px; height:auto; object-fit:contain; border:1px solid #ccc;");
                        } else if (originalStyle.contains("width:100px") || originalStyle.contains("width:100")) {
                            img.attr("style", "width:100px; height:80px; object-fit:cover; border-radius:8px;");
                        } else if (originalStyle.contains("width:120px") || originalStyle.contains("width:120")) {
                            img.attr("style", "width:120px; height:80px; object-fit:cover; border-radius:8px;");
                        } else {
                            img.attr("style", "max-width:100%; height:auto; object-fit:contain;");
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to convert image: " + src);
                        img.attr("src", "");
                    }
                }
                // Preserve existing data:image URLs
                else if (src.startsWith("data:image")) {
                    if (originalStyle.contains("max-height:60px") || originalStyle.contains("max-height:60")) {
                        img.attr("style", "height:60px; width:auto; object-fit:contain;");
                    } else if (originalStyle.contains("max-height:70px") || originalStyle.contains("max-height:70")) {
                        img.attr("style", "height:70px; width:auto; object-fit:contain;");
                    } else if (originalStyle.contains("width:400px") || originalStyle.contains("width:400")) {
                        img.attr("style", "width:400px; height:auto; object-fit:contain;");
                    } else {
                        img.attr("style", "max-width:100%; height:auto; object-fit:contain;");
                    }
                }
            }
        }
    }

    private String buildWordCompatibleWrapper(String content) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset='UTF-8'>\n" +
                "    <meta http-equiv='Content-Type' content='text/html; charset=utf-8'>\n" +
                "    <style>\n" +
                "        /* Complete CSS reset - matches PDF exactly */\n" +
                "        * {\n" +
                "            margin: 0;\n" +
                "            padding: 0;\n" +
                "            box-sizing: border-box;\n" +
                "        }\n" +
                "        \n" +
                "        body {\n" +
                "            font-family: 'Times New Roman', Times, serif, Arial, Helvetica, sans-serif;\n" +
                "            background: white;\n" +
                "            color: black;\n" +
                "            font-size: 12pt;\n" +
                "            line-height: 1.35;\n" +
                "            padding: 0.4in 0.3in;\n" +
                "            margin: 0;\n" +
                "        }\n" +
                "        \n" +
                "        /* Table styles - exact match with PDF */\n" +
                "        table {\n" +
                "            width: 100%;\n" +
                "            border-collapse: collapse;\n" +
                "            border: 1px solid #000000;\n" +
                "            font-size: 12pt;\n" +
                "            margin: 5px 0;\n" +
                "        }\n" +
                "        \n" +
                "        td, th {\n" +
                "            border: 1px solid #000000;\n" +
                "            padding: 4px 6px;\n" +
                "            vertical-align: top;\n" +
                "        }\n" +
                "        \n" +
                "        th {\n" +
                "            font-weight: bold;\n" +
                "            background-color: #f9f9f9;\n" +
                "        }\n" +
                "        \n" +
                "        /* Image styles - preserve exact dimensions */\n" +
                "        img {\n" +
                "            display: block;\n" +
                "            margin: 0 auto;\n" +
                "        }\n" +
                "        \n" +
                "        /* Signature row - side by side exactly like PDF */\n" +
                "        .signature-row {\n" +
                "            width: 100%;\n" +
                "            margin: 18px 0 10px 0;\n" +
                "            overflow: hidden;\n" +
                "        }\n" +
                "        \n" +
                "        .signature-left, .signature-right {\n" +
                "            width: 48%;\n" +
                "            float: left;\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "        \n" +
                "        .signature-right {\n" +
                "            float: right;\n" +
                "        }\n" +
                "        \n" +
                "        .signature-label {\n" +
                "            font-weight: bold;\n" +
                "            display: block;\n" +
                "            font-size: 12px;\n" +
                "            margin-top: 4px;\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "        \n" +
                "        .signature-name {\n" +
                "            display: block;\n" +
                "            font-size: 12px;\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "        \n" +
                "        .signature-space {\n" +
                "            min-height: 65px;\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "        \n" +
                "        /* Stamp box - exact like PDF */\n" +
                "        .stamp-box {\n" +
                "            border: 1.5px solid #000000;\n" +
                "            min-height: 75px;\n" +
                "            width: 220px;\n" +
                "            margin: 5px auto 0 auto;\n" +
                "            display: flex;\n" +
                "            align-items: center;\n" +
                "            justify-content: center;\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "        \n" +
                "        /* Two signatures in one row (Proforma-A) */\n" +
                "        .signature-item {\n" +
                "            width: 48%;\n" +
                "            display: inline-block;\n" +
                "            vertical-align: top;\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "        \n" +
                "        .signature-item:first-child {\n" +
                "            float: left;\n" +
                "        }\n" +
                "        \n" +
                "        .signature-item:last-child {\n" +
                "            float: right;\n" +
                "        }\n" +
                "        \n" +
                "        /* Agreement signature section */\n" +
                "        .signature-section, .witness-section {\n" +
                "            width: 100%;\n" +
                "            margin-top: 28px;\n" +
                "            overflow: hidden;\n" +
                "        }\n" +
                "        \n" +
                "        .box {\n" +
                "            width: 48%;\n" +
                "            float: left;\n" +
                "        }\n" +
                "        \n" +
                "        .box:last-child {\n" +
                "            float: right;\n" +
                "        }\n" +
                "        \n" +
                "        /* Aadhar images section */\n" +
                "        .aadhar-block {\n" +
                "            margin-top: 16px;\n" +
                "            border: 1.5px solid #000000;\n" +
                "            padding: 10px;\n" +
                "        }\n" +
                "        \n" +
                "        .aadhar-image-box {\n" +
                "            border: 2px dashed #444444;\n" +
                "            background: #f9f9f9;\n" +
                "            width: 320px;\n" +
                "            min-height: 210px;\n" +
                "            margin: 0 auto;\n" +
                "            padding: 8px;\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "        \n" +
                "        .aadhar-number-text {\n" +
                "            font-weight: bold;\n" +
                "            margin: 6px 0 10px 0;\n" +
                "        }\n" +
                "        \n" +
                "        /* MSEDCL section */\n" +
                "        .msedcl-section {\n" +
                "            margin: 15px 0 10px 0;\n" +
                "        }\n" +
                "        \n" +
                "        .msedcl-label {\n" +
                "            font-weight: bold;\n" +
                "            font-size: 12.5px;\n" +
                "            margin-bottom: 5px;\n" +
                "        }\n" +
                "        \n" +
                "        .msedcl-row {\n" +
                "            width: 100%;\n" +
                "            overflow: hidden;\n" +
                "            margin: 10px 0;\n" +
                "        }\n" +
                "        \n" +
                "        .msedcl-item {\n" +
                "            width: 48%;\n" +
                "            float: left;\n" +
                "        }\n" +
                "        \n" +
                "        /* Headings - exact match */\n" +
                "        h1, h2, .main-heading, .sub-heading, .re-header, .annexure, .proforma-title {\n" +
                "            text-align: center;\n" +
                "            font-weight: bold;\n" +
                "        }\n" +
                "        \n" +
                "        h1 {\n" +
                "            font-size: 15px;\n" +
                "            text-transform: uppercase;\n" +
                "            margin: 0 0 8px 0;\n" +
                "        }\n" +
                "        \n" +
                "        h2 {\n" +
                "            font-size: 14px;\n" +
                "            text-transform: uppercase;\n" +
                "            margin: 0 0 12px 0;\n" +
                "        }\n" +
                "        \n" +
                "        /* Page breaks */\n" +
                "        .page-break {\n" +
                "            page-break-before: always;\n" +
                "            break-before: page;\n" +
                "            margin: 0;\n" +
                "        }\n" +
                "        \n" +
                "        /* Clear floats */\n" +
                "        .clearfix {\n" +
                "            clear: both;\n" +
                "        }\n" +
                "        \n" +
                "        /* Text styles */\n" +
                "        .bold {\n" +
                "            font-weight: bold;\n" +
                "        }\n" +
                "        \n" +
                "        .text-right {\n" +
                "            text-align: right;\n" +
                "        }\n" +
                "        \n" +
                "        .center {\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "        \n" +
                "        .section-title {\n" +
                "            font-weight: bold;\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "        \n" +
                "        /* Paragraph text */\n" +
                "        p {\n" +
                "            margin: 6px 0;\n" +
                "            text-align: justify;\n" +
                "        }\n" +
                "        \n" +
                "        /* Photo grid for site photos */\n" +
                "        .photos-grid {\n" +
                "            display: block;\n" +
                "            width: 100%;\n" +
                "        }\n" +
                "        \n" +
                "        .photo-item {\n" +
                "            border: 1px solid #ddd;\n" +
                "            border-radius: 8px;\n" +
                "            padding: 15px;\n" +
                "            margin-bottom: 20px;\n" +
                "            background: #f9f9f9;\n" +
                "            page-break-inside: avoid;\n" +
                "        }\n" +
                "        \n" +
                "        .photo-caption {\n" +
                "            font-weight: bold;\n" +
                "            margin-bottom: 10px;\n" +
                "        }\n" +
                "        \n" +
                "        .photo-image {\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "        \n" +
                "        /* Module details section */\n" +
                "        .module-details {\n" +
                "            width: 470px;\n" +
                "            margin: 8px auto;\n" +
                "        }\n" +
                "        \n" +
                "        .detail-line {\n" +
                "            margin: 4px 0;\n" +
                "        }\n" +
                "        \n" +
                "        /* DCR signature section */\n" +
                "        .signature-details {\n" +
                "            width: 310px;\n" +
                "            text-align: left;\n" +
                "        }\n" +
                "        \n" +
                "        .signature-seal-text {\n" +
                "            width: 310px;\n" +
                "            font-weight: bold;\n" +
                "            margin-bottom: 6px;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                content + "\n" +
                "<div class='clearfix'></div>\n" +
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

            String finalHtml = buildWordCompatibleWrapper(
                    wcr + "\n" +
                            "<div class='page-break'></div>\n" +
                            annexure + "\n" +
                            "<div class='page-break'></div>\n" +
                            dcr + "\n" +
                            "<div class='page-break'></div>\n" +
                            agreement + "\n" +
                            "<div class='page-break'></div>\n" +
                            photos
            );

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

            // Remove scripts
            Elements scripts = doc.select("script");
            scripts.remove();

            // Convert all images to Base64 with proper sizing
            convertAllImagesToBase64(doc);

            return doc.body().html();
        } catch (Exception e) {
            System.err.println("Error processing: " + templateName + " - " + e.getMessage());
            return "<div>Error loading " + templateName + "</div>";
        }
    }
}