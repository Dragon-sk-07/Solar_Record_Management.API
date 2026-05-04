package com.suraj.Customer_Portal_29.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class PdfGeneratorService {

    private final TemplateEngine templateEngine;

    public PdfGeneratorService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] generatePdf(String type, Map<String, Object> data) {
        try {
            System.out.println("=== PDF Generation Started for: " + type + " ===");

            Context context = new Context();
            context.setVariables(data);

            String templateName = mapTemplateName(type);
            System.out.println("Template name: pdf/" + templateName);

            String html = templateEngine.process("pdf/" + templateName, context);
            System.out.println("HTML generated, length: " + html.length() + " characters");

            html = html.replace("\uFEFF", "");
            html = compressImagesInHtml(html);

            Document document = Jsoup.parse(html);
            document.outputSettings()
                    .syntax(Document.OutputSettings.Syntax.xml)
                    .escapeMode(Entities.EscapeMode.xhtml)
                    .charset("UTF-8");

            String xhtml = document.html();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(xhtml, null);
            builder.toStream(outputStream);
            builder.run();

            byte[] result = outputStream.toByteArray();
            System.out.println("PDF generated successfully for: " + type + ", size: " + result.length + " bytes");
            System.out.println("=== PDF Generation Completed for: " + type + " ===");

            return result;

        } catch (Exception e) {
            System.err.println("!!! PDF Generation FAILED for: " + type + " !!!");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("PDF generation failed for " + type + ": " + e.getMessage(), e);
        }
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

    private String compressImagesInHtml(String html) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("data:image/(\\w+);base64,([^\"]+)");
        java.util.regex.Matcher matcher = pattern.matcher(html);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String extension = matcher.group(1);
            String base64 = matcher.group(2);
            String compressed = compressBase64Image(base64, extension);
            matcher.appendReplacement(sb, "data:image/" + extension + ";base64," + compressed);
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    private String compressBase64Image(String base64, String extension) {
        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64);

            if (imageBytes.length < 200 * 1024) {
                return base64;
            }

            ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
            BufferedImage image = ImageIO.read(bais);

            if (image == null) {
                return base64;
            }

            int width = image.getWidth();
            int height = image.getHeight();
            int maxWidth = 800;

            if (width > maxWidth) {
                float ratio = (float) maxWidth / width;
                width = maxWidth;
                height = (int) (height * ratio);
            }

            java.awt.Image scaledImage = image.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
            BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            resized.getGraphics().drawImage(scaledImage, 0, 0, null);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            String format = extension.equals("png") ? "png" : "jpg";
            ImageIO.write(resized, format, baos);

            return Base64.getEncoder().encodeToString(baos.toByteArray());

        } catch (Exception e) {
            return base64;
        }
    }

    public static String imageToBase64(byte[] imageBytes, String mimeType) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
            BufferedImage image = ImageIO.read(bais);

            if (image != null) {
                int width = image.getWidth();
                int height = image.getHeight();
                int maxWidth = 800;

                if (width > maxWidth) {
                    float ratio = (float) maxWidth / width;
                    width = maxWidth;
                    height = (int) (height * ratio);

                    java.awt.Image scaledImage = image.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
                    BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                    resized.getGraphics().drawImage(scaledImage, 0, 0, null);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(resized, "jpg", baos);
                    return "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
                }
            }
        } catch (Exception e) {
        }

        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        return "data:" + mimeType + ";base64," + base64;
    }

    @Async
    public CompletableFuture<byte[]> generatePdfAsync(String type, Map<String, Object> data) {
        return CompletableFuture.completedFuture(generatePdf(type, data));
    }
}