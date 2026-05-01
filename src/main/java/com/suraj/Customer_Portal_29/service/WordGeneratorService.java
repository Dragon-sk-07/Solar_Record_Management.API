package com.suraj.Customer_Portal_29.service;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class WordGeneratorService {

    private final TemplateEngine templateEngine;

    public WordGeneratorService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    /**
     * Generates a Word document (.doc) that is editable in Microsoft Word
     * Uses HTML with Word-specific MIME type - lightweight and reliable
     */
    public byte[] generateWord(String templateName, Map<String, Object> data) {
        try {
            // Generate HTML from Thymeleaf template
            Context context = new Context();
            context.setVariables(data);
            String htmlContent = templateEngine.process("pdf/" + templateName, context);

            // Create Word-compatible HTML wrapper
            String wordHtml = buildWordHtml(htmlContent);

            // Convert to bytes with UTF-8 encoding
            return wordHtml.getBytes(StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("Word document generation failed: " + e.getMessage(), e);
        }
    }

    private String buildWordHtml(String content) {
        return """
            <!DOCTYPE html>
            <html xmlns:o='urn:schemas-microsoft-com:office:office' 
                  xmlns:w='urn:schemas-microsoft-com:office:word' 
                  xmlns='http://www.w3.org/TR/REC-html40'>
            <head>
                <meta charset="UTF-8">
                <title>Solar Installation Document</title>
                <style>
                    body {
                        font-family: Calibri, Arial, sans-serif;
                        font-size: 12pt;
                        margin: 2.54cm;  /* Standard Word margins */
                        line-height: 1.4;
                    }
                    table {
                        border-collapse: collapse;
                        width: 100%;
                        margin-bottom: 16px;
                    }
                    td, th {
                        border: 1px solid #000000;
                        padding: 6px 8px;
                        vertical-align: top;
                    }
                    .section-title {
                        font-weight: bold;
                        text-align: center;
                        background-color: #f0f0f0;
                    }
                    .value-field {
                        font-weight: normal;
                    }
                    .bold {
                        font-weight: bold;
                    }
                    .signature-row {
                        display: table;
                        width: 100%;
                        margin-top: 30px;
                    }
                    .signature-left, .signature-right {
                        display: table-cell;
                        width: 50%;
                        text-align: center;
                    }
                    .stamp-box {
                        border: 1px solid #000;
                        padding: 20px;
                        margin-top: 10px;
                        text-align: center;
                    }
                    .aadhar-block {
                        margin-top: 20px;
                        border: 1px solid #000;
                        padding: 15px;
                    }
                    .page-break {
                        page-break-before: always;
                    }
                    @media print {
                        body {
                            margin: 0;
                        }
                    }
                </style>
            </head>
            <body>
            """ + content + """
            </body>
            </html>
        """;
    }
}