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

            fixImagesForWord(doc);

            String finalHtml = buildWordWrapper(doc.html());
            return finalHtml.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Word generation failed: " + e.getMessage(), e);
        }
    }

    private void fixImagesForWord(Document doc) {
        Elements imgs = doc.select("img");
        for (Element img : imgs) {
            String originalStyle = img.attr("style");

            if (originalStyle.contains("max-height:60px") || originalStyle.contains("max-height:60")) {
                img.attr("style", "height:60px; width:auto;");
            } else if (originalStyle.contains("max-height:70px") || originalStyle.contains("max-height:70")) {
                img.attr("style", "height:70px; width:auto;");
            } else if (originalStyle.contains("max-height:50px") || originalStyle.contains("max-height:50")) {
                img.attr("style", "height:50px; width:auto;");
            } else if (originalStyle.contains("width:400px") || originalStyle.contains("width:400")) {
                img.attr("style", "width:400px; height:auto; border:1px solid #ccc;");
            } else if (originalStyle.contains("width:100px") || originalStyle.contains("width:100")) {
                img.attr("style", "width:100px; height:80px; border-radius:8px;");
            } else if (originalStyle.contains("width:120px") || originalStyle.contains("width:120")) {
                img.attr("style", "width:120px; height:80px; border-radius:8px;");
            } else {
                img.attr("style", "max-width:100%; height:auto;");
            }
        }
    }

    private String buildWordWrapper(String content) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset='UTF-8'>\n" +
                "    <style>\n" +
                "        * { margin:0; padding:0; }\n" +
                "        body { font-family: 'Times New Roman', Arial, sans-serif; font-size:12pt; line-height:1.35; margin:0.5in; padding:0; }\n" +
                "        table { width:100%; border-collapse:collapse; border:1px solid black; margin:5px 0; }\n" +
                "        td, th { border:1px solid black; padding:4px 6px; vertical-align:top; }\n" +
                "        img { max-width:100%; height:auto; display:block; margin:0 auto; }\n" +
                "        .signature-row { width:100%; margin:15px 0; overflow:hidden; }\n" +
                "        .signature-left, .signature-right { width:48%; float:left; text-align:center; }\n" +
                "        .signature-right { float:right; }\n" +
                "        .signature-space { min-height:60px; }\n" +
                "        .stamp-box { border:1.5px solid black; width:200px; margin:0 auto; text-align:center; padding:8px; }\n" +
                "        .page-break { page-break-before:always; }\n" +
                "        .clearfix { clear:both; }\n" +
                "        .aadhar-image-box { border:2px dashed #444; width:300px; margin:10px auto; padding:8px; text-align:center; }\n" +
                "        .box { width:48%; float:left; }\n" +
                "        .box:last-child { float:right; }\n" +
                "        .msedcl-row { overflow:hidden; }\n" +
                "        .msedcl-item { width:48%; float:left; }\n" +
                "        h1, h2, .re-header, .annexure { text-align:center; }\n" +
                "        .photo-item { margin-bottom:20px; page-break-inside:avoid; }\n" +
                "        .photo-image { text-align:center; }\n" +
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

            // Add page breaks between sections
            StringBuilder combined = new StringBuilder();
            combined.append(wcr);
            combined.append("<div style='page-break-before:always; height:1px;'></div>");
            combined.append(annexure);
            combined.append("<div style='page-break-before:always; height:1px;'></div>");
            combined.append(dcr);
            combined.append("<div style='page-break-before:always; height:1px;'></div>");
            combined.append(agreement);
            combined.append("<div style='page-break-before:always; height:1px;'></div>");
            combined.append(photos);

            String finalHtml = buildWordWrapper(combined.toString());

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