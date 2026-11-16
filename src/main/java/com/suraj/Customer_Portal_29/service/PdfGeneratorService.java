package com.suraj.Customer_Portal_29.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

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
            Context context = new Context();
            context.setVariables(data);

            String templateName = mapTemplateName(type);
            String html = templateEngine.process("pdf/" + templateName, context);

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

            long targetBytes = "all-in-one".equalsIgnoreCase(type) ? (2500L * 1024L) : (300L * 1024L);

            if (result.length > targetBytes) {
                result = forceStrictCompression(result, targetBytes);
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed for " + type + ": " + e.getMessage(), e);
        }
    }

    private byte[] forceStrictCompression(byte[] pdfBytes, long targetBytes) {
        int maxWidth = 700;

        for (int attempt = 0; attempt < 6; attempt++) {
            PDDocument document = null;
            try {
                document = PDDocument.load(pdfBytes);

                for (PDPage page : document.getPages()) {
                    PDResources resources = page.getResources();
                    if (resources != null) {
                        for (COSName name : resources.getXObjectNames()) {
                            PDXObject xobject = resources.getXObject(name);
                            if (xobject instanceof PDImageXObject) {
                                PDImageXObject image = (PDImageXObject) xobject;
                                BufferedImage bufferedImage = image.getImage();

                                if (bufferedImage != null) {
                                    int newWidth = Math.min(bufferedImage.getWidth(), maxWidth);
                                    int newHeight = (int) ((double) bufferedImage.getHeight() * newWidth / bufferedImage.getWidth());

                                    BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
                                    scaledImage.getGraphics().drawImage(bufferedImage.getScaledInstance(newWidth, newHeight, java.awt.Image.SCALE_SMOOTH), 0, 0, null);

                                    ByteArrayOutputStream imgBaos = new ByteArrayOutputStream();
                                    ImageIO.write(scaledImage, "jpeg", imgBaos);

                                    PDImageXObject newImage = PDImageXObject.createFromByteArray(document, imgBaos.toByteArray(), name.getName());
                                    resources.put(name, newImage);

                                    imgBaos.close();
                                    bufferedImage.flush();
                                    scaledImage.flush();
                                }
                            }
                        }
                    }
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                document.setAllSecurityToBeRemoved(true);
                document.save(baos);

                byte[] compressedBytes = baos.toByteArray();
                baos.close();

                if (compressedBytes.length <= targetBytes) {
                    return compressedBytes;
                }

                pdfBytes = compressedBytes;
                maxWidth = (int) (maxWidth * 0.6);

            } catch (Exception e) {
                break;
            } finally {
                if (document != null) {
                    try { document.close(); } catch (Exception ignored) {}
                }
            }
        }
        return pdfBytes;
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
            case "site-photos":
                return "Site-photos";
            case "SOLAR_PROPOSAL_11KW":
                return "SOLAR_PROPOSAL_11KW";
            case "METER_TESTING_REQUEST":
                return "METER_TESTING_REQUEST";
            case "PM_SURYA_GHAR_AGREEMENT":
                return "PM_SURYA_GHAR_AGREEMENT";
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
            int maxWidth = 600;

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
                int maxWidth = 600;

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