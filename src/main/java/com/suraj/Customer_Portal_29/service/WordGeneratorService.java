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
            // Generate HTML from Thymeleaf template (same as PDF)
            Context context = new Context();
            context.setVariables(data);
            String htmlContent = templateEngine.process("pdf/" + templateName, context);

            // Parse HTML with Jsoup
            Document doc = Jsoup.parse(htmlContent);

            // FIX 1: Convert flexbox signature rows to Word-compatible tables
            Elements signatureRows = doc.select(".signature-row");
            for (Element row : signatureRows) {
                Element table = new Element("table");
                table.attr("style", "width:100%; margin-top:30px; border:none;");

                Element tr = new Element("tr");
                Element leftTd = new Element("td");
                Element rightTd = new Element("td");

                leftTd.attr("style", "width:50%; text-align:center; border:none; vertical-align:top;");
                rightTd.attr("style", "width:50%; text-align:center; border:none; vertical-align:top;");

                Element leftDiv = row.select(".signature-left").first();
                Element rightDiv = row.select(".signature-right").first();

                if (leftDiv != null) {
                    leftTd.html(leftDiv.html());
                }
                if (rightDiv != null) {
                    rightTd.html(rightDiv.html());
                }

                tr.appendChild(leftTd);
                tr.appendChild(rightTd);
                table.appendChild(tr);
                row.replaceWith(table);
            }

            // FIX 2: Remove all flexbox/modern CSS that Word doesn't support
            doc.select("[style*='display:flex']").removeAttr("style");
            doc.select("[style*='display: table']").removeAttr("style");
            doc.select("[style*='flex-direction']").removeAttr("style");
            doc.select("[style*='align-items']").removeAttr("style");
            doc.select("[style*='justify-content']").removeAttr("style");
            doc.select("[style*='gap']").removeAttr("style");

            // FIX 3: Fix aadhar images container
            Elements containers = doc.select(".aadhar-images-container");
            for (Element container : containers) {
                container.attr("style", "width:100%; text-align:center; margin:10px 0;");
            }

            // FIX 4: Ensure images display properly
            Elements images = doc.select("img");
            for (Element img : images) {
                img.attr("style", "max-width:100%; height:auto; margin:10px auto; display:block;");
            }

            // FIX 5: Convert page-break div to Word compatible
            Elements pageBreaks = doc.select(".page-break");
            for (Element pb : pageBreaks) {
                pb.attr("style", "page-break-before: always;");
            }

            // FIX 6: Ensure all tables have proper borders
            Elements tables = doc.select("table");
            for (Element table : tables) {
                if (!table.hasAttr("style")) {
                    table.attr("style", "border-collapse: collapse; width: 100%; margin-bottom: 16px;");
                }
                Elements tds = table.select("td, th");
                for (Element td : tds) {
                    if (!td.hasAttr("style") || !td.attr("style").contains("border")) {
                        String existingStyle = td.hasAttr("style") ? td.attr("style") : "";
                        td.attr("style", existingStyle + " border: 1px solid #000000; padding: 6px 8px; vertical-align: top;");
                    }
                }
            }

            // FIX 7: Fix stamp box
            Elements stampBoxes = doc.select(".stamp-box");
            for (Element stamp : stampBoxes) {
                stamp.attr("style", "border: 1px solid #000000; padding: 20px; margin-top: 10px; text-align: center; width: 220px; margin-left: auto; margin-right: auto;");
            }

            // FIX 8: Fix aadhar block
            Elements aadharBlocks = doc.select(".aadhar-block");
            for (Element block : aadharBlocks) {
                block.attr("style", "margin-top: 20px; border: 1px solid #000000; padding: 15px;");
            }

            // FIX 9: Fix aadhar image box
            Elements imageBoxes = doc.select(".aadhar-image-box");
            for (Element box : imageBoxes) {
                box.attr("style", "border: 2px dashed #444444; background: #f9f9f9; width: 320px; margin: 10px auto; padding: 8px; text-align: center;");
            }

            // FIX 10: Remove any remaining Thymeleaf attributes (already processed but safe)
            doc.select("[th\\:text], [th\\:value], [th\\:field], [th\\:each], [th\\:if], [th\\:src]")
                    .forEach(el -> {
                        el.removeAttr("th:text");
                        el.removeAttr("th:value");
                        el.removeAttr("th:field");
                        el.removeAttr("th:each");
                        el.removeAttr("th:if");
                        el.removeAttr("th:src");
                    });

            // Get cleaned HTML
            String cleanedHtml = doc.html();

            // Build final Word document with proper Word-compatible HTML
            String wordHtml = buildWordCompatibleHtml(cleanedHtml);

            return wordHtml.getBytes(StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("Word document generation failed: " + e.getMessage(), e);
        }
    }

    private String buildWordCompatibleHtml(String content) {
        return "<!DOCTYPE html>\n" +
                "<html xmlns:o='urn:schemas-microsoft-com:office:office' \n" +
                "      xmlns:w='urn:schemas-microsoft-com:office:word' \n" +
                "      xmlns:m='http://schemas.microsoft.com/office/2004/12/omml'\n" +
                "      xmlns='http://www.w3.org/TR/REC-html40'>\n" +
                "<head>\n" +
                "    <meta charset='UTF-8'>\n" +
                "    <title>Solar Installation Document</title>\n" +
                "    <style>\n" +
                "        body {\n" +
                "            font-family: Arial, Helvetica, sans-serif;\n" +
                "            font-size: 12pt;\n" +
                "            margin: 2.54cm;\n" +
                "            line-height: 1.4;\n" +
                "        }\n" +
                "        table {\n" +
                "            border-collapse: collapse;\n" +
                "            width: 100%;\n" +
                "            margin-bottom: 16px;\n" +
                "        }\n" +
                "        td, th {\n" +
                "            border: 1px solid #000000;\n" +
                "            padding: 6px 8px;\n" +
                "            vertical-align: top;\n" +
                "        }\n" +
                "        .section-title {\n" +
                "            font-weight: bold;\n" +
                "            text-align: center;\n" +
                "            background-color: #f0f0f0;\n" +
                "        }\n" +
                "        .value-field {\n" +
                "            font-weight: normal;\n" +
                "        }\n" +
                "        .bold {\n" +
                "            font-weight: bold;\n" +
                "        }\n" +
                "        .stamp-box {\n" +
                "            border: 1px solid #000000;\n" +
                "            padding: 20px;\n" +
                "            margin-top: 10px;\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "        .aadhar-block {\n" +
                "            margin-top: 20px;\n" +
                "            border: 1px solid #000000;\n" +
                "            padding: 15px;\n" +
                "        }\n" +
                "        .aadhar-image-box {\n" +
                "            border: 2px dashed #444444;\n" +
                "            background: #f9f9f9;\n" +
                "            width: 320px;\n" +
                "            margin: 10px auto;\n" +
                "            padding: 8px;\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "        .aadhar-image {\n" +
                "            max-width: 100%;\n" +
                "            height: auto;\n" +
                "            border: 1px solid #aaaaaa;\n" +
                "            border-radius: 5px;\n" +
                "        }\n" +
                "        .page-break {\n" +
                "            page-break-before: always;\n" +
                "        }\n" +
                "        .text-right {\n" +
                "            text-align: right;\n" +
                "        }\n" +
                "        h1, h2 {\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "        .gov-wrapper {\n" +
                "            max-width: 1100px;\n" +
                "            margin: 0 auto;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" + content + "\n" +
                "</body>\n" +
                "</html>";
    }
}