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

            Context context = new Context();
            context.setVariables(data);

            String htmlContent =
                    templateEngine.process("pdf/" + templateName, context);

            Document doc = Jsoup.parse(htmlContent);

            applyInlineStyles(doc);
            convertSignatureRows(doc);
            removeThymeleafAttributes(doc);

            String finalHtml = doc.body().html();

            String wordHtml = buildWordWrapper(finalHtml);

            return wordHtml.getBytes(StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Word document generation failed: " + e.getMessage(), e
            );
        }
    }

    private void applyInlineStyles(Document doc) {

        Elements body = doc.select("body");
        for (Element el : body) {
            el.attr("style",
                    "font-family:Arial,Helvetica,sans-serif;" +
                            "font-size:12px;" +
                            "color:#000;" +
                            "line-height:1.35;" +
                            "margin:0;");
        }

        Elements wrappers = doc.select(".gov-wrapper");
        for (Element el : wrappers) {
            el.attr("style",
                    "width:100%;" +
                            "margin:0 auto;");
        }

        Elements h1 = doc.select("h1");
        for (Element el : h1) {
            el.attr("style",
                    "font-size:15px;" +
                            "font-weight:bold;" +
                            "text-align:center;" +
                            "text-transform:uppercase;" +
                            "margin:0 0 8px 0;");
        }

        Elements h2 = doc.select("h2");
        for (Element el : h2) {
            el.attr("style",
                    "font-size:14px;" +
                            "font-weight:bold;" +
                            "text-align:center;" +
                            "text-transform:uppercase;" +
                            "margin:0 0 12px 0;");
        }

        Elements tables = doc.select("table");
        for (Element table : tables) {
            table.attr("style",
                    "width:100%;" +
                            "border-collapse:collapse;" +
                            "table-layout:fixed;" +
                            "font-size:12px;" +
                            "margin-bottom:10px;");
        }

        Elements rows = doc.select("tr");
        for (Element row : rows) {
            row.attr("style", "page-break-inside:avoid;");
        }

        Elements cells = doc.select("td, th");
        for (Element cell : cells) {
            cell.attr("style",
                    "border:1px solid #000;" +
                            "padding:3px 4px;" +
                            "vertical-align:top;" +
                            "word-wrap:break-word;");
        }

        Elements sr = doc.select(".col-srno");
        for (Element el : sr) {
            el.attr("style",
                    "width:6%;" +
                            "text-align:center;" +
                            "font-weight:bold;" +
                            "background:#efefef;" +
                            "border:1px solid #000;" +
                            "padding:3px 4px;");
        }

        Elements obs = doc.select(".col-obs");
        for (Element el : obs) {
            el.attr("style",
                    "width:38%;" +
                            "font-weight:bold;" +
                            "border:1px solid #000;" +
                            "padding:3px 4px;");
        }

        Elements val = doc.select(".col-value");
        for (Element el : val) {
            el.attr("style",
                    "width:56%;" +
                            "border:1px solid #000;" +
                            "padding:3px 4px;");
        }

        Elements titles = doc.select(".section-title");
        for (Element el : titles) {
            el.attr("style",
                    "font-weight:bold;" +
                            "text-align:center;" +
                            "font-size:13px;" +
                            "background:#efefef;" +
                            "border:1px solid #000;" +
                            "padding:4px;");
        }

        Elements valueFields = doc.select(".value-field");
        for (Element el : valueFields) {
            el.attr("style", "font-weight:600;");
        }

        Elements bold = doc.select(".bold");
        for (Element el : bold) {
            el.attr("style", "font-weight:bold;");
        }

        Elements paras =
                doc.select(".undertaking-p1, .undertaking-p2, .guarantee-text");
        for (Element el : paras) {
            el.attr("style",
                    "margin:8px 0;" +
                            "text-align:justify;" +
                            "font-size:12px;" +
                            "line-height:1.45;");
        }

        Elements pageBreak = doc.select(".page-break");
        for (Element el : pageBreak) {
            el.attr("style",
                    "page-break-before:always;" +
                            "height:0;");
        }

        Elements stamp = doc.select(".stamp-box");
        for (Element el : stamp) {
            el.attr("style",
                    "border:1px solid #000;" +
                            "width:220px;" +
                            "height:75px;" +
                            "text-align:center;" +
                            "vertical-align:middle;" +
                            "margin:0 auto;" +
                            "padding-top:28px;" +
                            "font-size:11px;");
        }

        Elements aadharBlock = doc.select(".aadhar-block");
        for (Element el : aadharBlock) {
            el.attr("style",
                    "margin-top:16px;" +
                            "border:1px solid #000;" +
                            "padding:10px;");
        }

        Elements aadharText = doc.select(".aadhar-number-text");
        for (Element el : aadharText) {
            el.attr("style",
                    "font-weight:bold;" +
                            "margin:6px 0 10px 0;");
        }

        Elements imgBoxes = doc.select(".aadhar-image-box");
        for (Element el : imgBoxes) {
            el.attr("style",
                    "width:320px;" +
                            "border:2px dashed #444;" +
                            "padding:8px;" +
                            "margin:0 auto 14px auto;" +
                            "text-align:center;");
        }

        Elements imgs = doc.select("img");
        for (Element img : imgs) {
            img.attr("style",
                    "width:280px;" +
                            "height:auto;" +
                            "display:block;" +
                            "margin:0 auto;" +
                            "border:1px solid #999;");
        }

        Elements right = doc.select(".text-right");
        for (Element el : right) {
            el.attr("style", "text-align:right; margin-top:8px;");
        }
    }

    private void convertSignatureRows(Document doc) {

        Elements rows = doc.select(".signature-row");

        for (Element row : rows) {

            Element left = row.select(".signature-left").first();
            Element right = row.select(".signature-right").first();

            Element table = new Element("table");
            table.attr("style",
                    "width:100%;" +
                            "border:none;" +
                            "table-layout:fixed;" +
                            "margin-top:18px;" +
                            "margin-bottom:10px;");

            Element tr = new Element("tr");

            Element td1 = new Element("td");
            td1.attr("style",
                    "width:50%;" +
                            "border:none;" +
                            "vertical-align:top;" +
                            "text-align:center;" +
                            "padding-right:8px;");

            Element td2 = new Element("td");
            td2.attr("style",
                    "width:50%;" +
                            "border:none;" +
                            "vertical-align:top;" +
                            "text-align:center;" +
                            "padding-left:8px;");

            if (left != null) {
                td1.html(
                        "<div style='height:55px;'></div>" +
                                left.html()
                );
            }

            if (right != null) {
                td2.html(right.html());
            }

            tr.appendChild(td1);
            tr.appendChild(td2);
            table.appendChild(tr);

            row.replaceWith(table);
        }
    }

    private void removeThymeleafAttributes(Document doc) {

        Elements all = doc.getAllElements();

        for (Element el : all) {
            el.attributes().asList().forEach(attr -> {
                if (attr.getKey().startsWith("th:")) {
                    el.removeAttr(attr.getKey());
                }
            });
        }
    }

    private String buildWordWrapper(String content) {

        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<title>Document</title>" +
                "</head>" +
                "<body style='font-family:Arial,Helvetica,sans-serif;" +
                "font-size:12px;" +
                "margin:1cm;'>" +
                content +
                "</body>" +
                "</html>";
    }
}