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
            case "FrontSix":
                return "FrontSix";
            case "wcr":
                return "WCR_Undertaking_Guarantee_Aadhar";
            case "proforma-a":
                return "Annexure-I_Proforma-A";
            case "dcr":
                return "Declaration_FOR_DCR";
            case "agreement":
                return "NET_METERING_CONNECTION_AGREEMENT";
            case "indemnity":
                return "INDEMNITY_BOND";
            case "site-photos":
                return "Site-photos";
            default:
                return type;
        }
    }

    public byte[] generateWord(String type, Map<String, Object> data) {
        try {
            Context context = new Context();
            context.setVariables(data);
            String templateName = mapTemplateName(type);
            String htmlContent = templateEngine.process("pdf/" + templateName, context);
            Document doc = Jsoup.parse(htmlContent);

            // Keep original URLs - Word will load images from Cloudinary
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
            String src = img.attr("src");
            String originalStyle = img.attr("style");

            // Keep original URL (no Base64 conversion)
            // Just preserve original dimensions from style
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

            img.attr("width", "auto");
            img.attr("height", "auto");
            img.attr("border", "0");
        }
    }

    private String buildWordWrapper(String content) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset='UTF-8'>\n" +
                "    <style>\n" +
                "        * { margin:0; padding:0; box-sizing:border-box; }\n" +
                "        body { font-family: 'Times New Roman', Arial, sans-serif; background:white; color:black; font-size:12pt; line-height:1.35; padding:0.4in 0.3in; margin:0; }\n" +
                "        table { width:100%; border-collapse:collapse; border:1px solid #000000; font-size:12pt; margin:5px 0; }\n" +
                "        td, th { border:1px solid #000000; padding:4px 6px; vertical-align:top; }\n" +
                "        th { font-weight:bold; background-color:#f9f9f9; }\n" +
                "        img { max-width:100%; height:auto; display:block; margin:0 auto; }\n" +
                "        .signature-row { width:100%; margin:18px 0 10px 0; overflow:hidden; }\n" +
                "        .signature-left, .signature-right { width:48%; float:left; text-align:center; }\n" +
                "        .signature-right { float:right; }\n" +
                "        .signature-label { font-weight:bold; display:block; font-size:12px; margin-top:4px; text-align:center; }\n" +
                "        .signature-name { display:block; font-size:12px; text-align:center; }\n" +
                "        .signature-space { min-height:65px; text-align:center; }\n" +
                "        .stamp-box { border:1.5px solid #000000; min-height:75px; width:220px; margin:5px auto 0 auto; display:flex; align-items:center; justify-content:center; text-align:center; }\n" +
                "        .signature-item { width:48%; display:inline-block; vertical-align:top; text-align:center; }\n" +
                "        .signature-item:first-child { float:left; }\n" +
                "        .signature-item:last-child { float:right; }\n" +
                "        .signature-section, .witness-section { width:100%; margin-top:28px; overflow:hidden; }\n" +
                "        .box { width:48%; float:left; }\n" +
                "        .box:last-child { float:right; }\n" +
                "        .aadhar-block { margin-top:16px; border:1.5px solid #000000; padding:10px; }\n" +
                "        .aadhar-image-box { border:2px dashed #444444; background:#f9f9f9; width:320px; min-height:210px; margin:0 auto; padding:8px; text-align:center; }\n" +
                "        .aadhar-number-text { font-weight:bold; margin:6px 0 10px 0; }\n" +
                "        .msedcl-section { margin:15px 0 10px 0; }\n" +
                "        .msedcl-label { font-weight:bold; font-size:12.5px; margin-bottom:5px; }\n" +
                "        .msedcl-row { width:100%; overflow:hidden; margin:10px 0; }\n" +
                "        .msedcl-item { width:48%; float:left; }\n" +
                "        h1, h2, .main-heading, .sub-heading, .re-header, .annexure, .proforma-title { text-align:center; font-weight:bold; }\n" +
                "        h1 { font-size:15px; text-transform:uppercase; margin:0 0 8px 0; }\n" +
                "        h2 { font-size:14px; text-transform:uppercase; margin:0 0 12px 0; }\n" +
                "        .page-break { page-break-before:always; break-before:page; margin:0; }\n" +
                "        .clearfix { clear:both; }\n" +
                "        .bold { font-weight:bold; }\n" +
                "        .text-right { text-align:right; }\n" +
                "        .center { text-align:center; }\n" +
                "        .section-title { font-weight:bold; text-align:center; }\n" +
                "        p { margin:6px 0; text-align:justify; }\n" +
                "        .photos-grid { display:block; width:100%; }\n" +
                "        .photo-item { border:1px solid #ddd; border-radius:8px; padding:15px; margin-bottom:20px; background:#f9f9f9; page-break-inside:avoid; }\n" +
                "        .photo-caption { font-weight:bold; margin-bottom:10px; }\n" +
                "        .photo-image { text-align:center; }\n" +
                "        .module-details { width:470px; margin:8px auto; }\n" +
                "        .detail-line { margin:4px 0; }\n" +
                "        .signature-details { width:310px; text-align:left; }\n" +
                "        .signature-seal-text { width:310px; font-weight:bold; margin-bottom:6px; }\n" +
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
            // Generate all templates including FrontSix
            String frontSix = processTemplate("FrontSix", data);
            String wcr = processTemplate("WCR_Undertaking_Guarantee_Aadhar", data);
            String annexure = processTemplate("Annexure-I_Proforma-A", data);
            String dcr = processTemplate("Declaration_FOR_DCR", data);
            String agreement = processTemplate("NET_METERING_CONNECTION_AGREEMENT", data);
            String photos = processTemplate("Site-photos", data);

            // Build combined HTML with page breaks
            String finalHtml = buildWordWrapper(
                    frontSix + "\n" +
                            "<div class='page-break'></div>\n" +
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

            Elements scripts = doc.select("script");
            scripts.remove();

            // Keep original URLs - Word will load images from Cloudinary
            fixImagesForWord(doc);

            return doc.body().html();
        } catch (Exception e) {
            System.err.println("Error processing: " + templateName + " - " + e.getMessage());
            return "<div>Error loading " + templateName + "</div>";
        }
    }
}