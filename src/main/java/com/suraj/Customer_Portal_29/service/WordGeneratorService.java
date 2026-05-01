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

    public byte[] generateWord(String templateName, Map<String, Object> data) {
        try {
            // Generate HTML with Thymeleaf (values get populated here)
            Context context = new Context();
            context.setVariables(data);
            String htmlContent = templateEngine.process("pdf/" + templateName, context);

            // DEBUG: Print first 500 chars to verify values
            System.out.println("Generated HTML (first 500 chars): " + htmlContent.substring(0, Math.min(500, htmlContent.length())));

            // Convert all CSS classes to inline styles for Word compatibility
            Document doc = Jsoup.parse(htmlContent);

            // Apply inline styles to all elements
            applyInlineStyles(doc);

            // Convert flexbox layouts to tables
            convertFlexToTables(doc);

            // Ensure all th:* attributes are removed (values are already populated)
            removeThymeleafAttributes(doc);

            String finalHtml = doc.html();

            // Wrap with Word-compatible HTML
            String wordHtml = buildWordWrapper(finalHtml);

            return wordHtml.getBytes(StandardCharsets.UTF_8);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Word document generation failed: " + e.getMessage(), e);
        }
    }

    private void applyInlineStyles(Document doc) {
        // Tables
        Elements tables = doc.select("table");
        for (Element table : tables) {
            table.attr("style", "border-collapse: collapse; width: 100%; margin-bottom: 16px;");
        }

        // Table cells
        Elements cells = doc.select("td, th");
        for (Element cell : cells) {
            cell.attr("style", "border: 1px solid #000000; padding: 6px 8px; vertical-align: top;");
        }

        // Section titles
        Elements sectionTitles = doc.select(".section-title");
        for (Element el : sectionTitles) {
            el.attr("style", "font-weight: bold; text-align: center; background-color: #f0f0f0;");
        }

        // Value fields
        Elements valueFields = doc.select(".value-field");
        for (Element el : valueFields) {
            el.attr("style", "font-weight: normal;");
        }

        // Bold text
        Elements boldText = doc.select(".bold");
        for (Element el : boldText) {
            el.attr("style", "font-weight: bold;");
        }

        // Aadhar block
        Elements aadharBlocks = doc.select(".aadhar-block");
        for (Element el : aadharBlocks) {
            el.attr("style", "margin-top: 20px; border: 1px solid #000000; padding: 15px;");
        }

        // Aadhar image box
        Elements imageBoxes = doc.select(".aadhar-image-box");
        for (Element el : imageBoxes) {
            el.attr("style", "border: 2px dashed #444444; background: #f9f9f9; width: 320px; margin: 10px auto; padding: 8px; text-align: center;");
        }

        // Images
        Elements images = doc.select("img");
        for (Element img : images) {
            img.attr("style", "max-width: 100%; height: auto; display: block; margin: 10px auto;");
        }

        // Headers
        Elements h1 = doc.select("h1");
        for (Element el : h1) {
            el.attr("style", "font-size: 15px; font-weight: bold; text-align: center; text-transform: uppercase; margin: 0 0 8px 0;");
        }

        Elements h2 = doc.select("h2");
        for (Element el : h2) {
            el.attr("style", "font-size: 14px; font-weight: bold; text-align: center; text-transform: uppercase; margin: 0 0 12px 0;");
        }

        // Paragraphs
        Elements paragraphs = doc.select(".undertaking-p1, .undertaking-p2, .guarantee-text");
        for (Element el : paragraphs) {
            el.attr("style", "margin: 8px 0; text-align: justify; font-size: 12.5px; line-height: 1.45;");
        }

        // Page break
        Elements pageBreaks = doc.select(".page-break");
        for (Element el : pageBreaks) {
            el.attr("style", "page-break-before: always;");
        }
    }

    private void convertFlexToTables(Document doc) {
        Elements signatureRows = doc.select(".signature-row");
        for (Element row : signatureRows) {
            Element leftDiv = row.select(".signature-left").first();
            Element rightDiv = row.select(".signature-right").first();

            Element table = new Element("table");
            table.attr("style", "width:100%; margin-top:30px; border:none;");

            Element tr = new Element("tr");

            Element leftTd = new Element("td");
            leftTd.attr("style", "width:50%; text-align:center; border:none; vertical-align:top;");
            if (leftDiv != null) {
                leftTd.html(leftDiv.html());
            }

            Element rightTd = new Element("td");
            rightTd.attr("style", "width:50%; text-align:center; border:none; vertical-align:top;");
            if (rightDiv != null) {
                rightTd.html(rightDiv.html());
            }

            tr.appendChild(leftTd);
            tr.appendChild(rightTd);
            table.appendChild(tr);
            row.replaceWith(table);
        }
    }

    private void removeThymeleafAttributes(Document doc) {
        Elements elements = doc.select("[th\\:text], [th\\:value], [th\\:field], [th\\:each], [th\\:if], [th\\:src], [th\\:alt]");
        for (Element el : elements) {
            el.removeAttr("th:text");
            el.removeAttr("th:value");
            el.removeAttr("th:field");
            el.removeAttr("th:each");
            el.removeAttr("th:if");
            el.removeAttr("th:src");
            el.removeAttr("th:alt");
        }
    }

    private String buildWordWrapper(String content) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset='UTF-8'>\n" +
                "    <title>Solar Installation Document</title>\n" +
                "</head>\n" +
                "<body style='font-family: Arial, Helvetica, sans-serif; font-size: 12pt; margin: 2.54cm; line-height: 1.4;'>\n" +
                content + "\n" +
                "</body>\n" +
                "</html>";
    }
}