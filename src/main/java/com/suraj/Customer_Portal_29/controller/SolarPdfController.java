package com.suraj.Customer_Portal_29.controller;

import com.suraj.Customer_Portal_29.dto.response.SolarRecordResponseDto;
import com.suraj.Customer_Portal_29.entity.Owner;
import com.suraj.Customer_Portal_29.entity.Permission;
import com.suraj.Customer_Portal_29.entity.UserRole;
import com.suraj.Customer_Portal_29.service.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import com.suraj.Customer_Portal_29.repository.OwnerRepository;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/solar/pdf")
public class SolarPdfController {

    private final SolarRecordService solarService;
    private final PdfGeneratorService pdfService;
    private final WordGeneratorService wordService;
    private final PdfMergerService pdfMergerService;
    private final OwnerRepository ownerRepository;

    private static final String IMAGE_COTATION_1ST_PAGE = "Cotation_1stPage Image.png";
    private static final String IMAGE_SECOND_PAGE_FIRST = "SecondPageFirst Image.png";
    private static final String IMAGE_SECOND_PAGE_SECOND = "SecondPageSecond Image.png";
    private static final String IMAGE_SECOND_PAGE_THIRD = "SecondPageThird Image.png";
    private static final String IMAGE_SECOND_PAGE_FOURTH = "SecondPageFourth Image.png";

    public SolarPdfController(SolarRecordService solarService,
                              PdfGeneratorService pdfService,
                              WordGeneratorService wordService,
                              PdfMergerService pdfMergerService,
                              OwnerRepository ownerRepository) {
        this.solarService = solarService;
        this.pdfService = pdfService;
        this.wordService = wordService;
        this.pdfMergerService = pdfMergerService;
        this.ownerRepository = ownerRepository;
    }

    @GetMapping("/{id}/{type}/word")
    public ResponseEntity<byte[]> downloadWord(@PathVariable String id, @PathVariable String type) {
        checkDownloadPermission("word");
        Map<String, Object> data = buildData(id, false);
        byte[] wordDoc = wordService.generateWord(type, data);
        String filename = type + ".doc";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.TEXT_HTML)
                .body(wordDoc);
    }

    @GetMapping("/{id}/{type}")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable String id, @PathVariable String type) {
        checkDownloadPermission("pdf");
        Map<String, Object> data = buildData(id, true);
        byte[] pdf = pdfService.generatePdfAsync(type, data).join();
        String filename = getPdfFilename(type);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/{id}/all-in-one")
    public ResponseEntity<byte[]> downloadAllInOnePdf(@PathVariable String id) {
        checkDownloadPermission("pdf");
        try {
            Map<String, Object> data = buildData(id, true);
            List<byte[]> pdfs = new ArrayList<>();
            String[] pdfTypes = {"METER_TESTING_REQUEST", "FrontSix", "wcr", "proforma-a", "dcr", "agreement", "site-photos"};

            for (String type : pdfTypes) {
                try {
                    byte[] pdf = pdfService.generatePdf(type, data);
                    if (pdf != null && pdf.length > 500) {
                        pdfs.add(pdf);
                    }
                } catch (Exception e) {
                    System.err.println("Failed: " + type + " - " + e.getMessage());
                }
            }

            if (!pdfs.isEmpty()) {
                byte[] mergedPdf = pdfMergerService.mergePdfs(pdfs);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=All_In_One.pdf")
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(mergedPdf);
            }

            byte[] wordDoc = wordService.generateCombinedWord(data);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=All_In_One.doc")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(wordDoc);

        } catch (Exception e) {
            Map<String, Object> data = buildData(id, true);
            byte[] wordDoc = wordService.generateCombinedWord(data);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=All_In_One.doc")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(wordDoc);
        }
    }

    @GetMapping("/{id}/all-in-one/word")
    public ResponseEntity<byte[]> downloadAllInOneWord(@PathVariable String id) {
        checkDownloadPermission("word");
        try {
            Map<String, Object> data = buildData(id, true);
            byte[] wordDoc = wordService.generateCombinedWord(data);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=All_In_One.doc")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(wordDoc);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("All In One Word Failed: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> buildData(String id, boolean includeCompleteData) {
        SolarRecordResponseDto record = solarService.findById(id);
        Map<String, Object> data = new HashMap<>();
        LocalDate now = LocalDate.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM");

        String installationDate = getValueOrDefault(record.getInstallationDate(),
                now.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));

        // Customer & Site Information
        data.put("name", getValueOrDefault(record.getName(), ""));
        data.put("consumerNumber", getValueOrDefault(record.getConsumerNumber(), ""));
        data.put("consumerName", getValueOrDefault(record.getName(), ""));
        data.put("mobileNumber", getValueOrDefault(record.getMobileNumber(), ""));
        data.put("email", getValueOrDefault(record.getEmail(), ""));
        data.put("siteAddress", getValueOrDefault(record.getSiteAddress(), ""));
        data.put("aadharNumber", getValueOrDefault(record.getAadharNumber(), ""));
        data.put("place", getValueOrDefault(record.getPlace(), ""));
        data.put("applicationNumber", getValueOrDefault(record.getApplicationNumber(), ""));
        data.put("applicationDate", getValueOrDefault(record.getApplicationDate(), ""));
        data.put("sanctionNumber", getValueOrDefault(record.getSanctionNumber(), ""));
        data.put("billingUnit", getValueOrDefault(record.getBillingUnit(), ""));
        data.put("connectionType", getValueOrDefault(record.getConnectionType(), ""));

        // Technical Details
        data.put("cellManufacturerName", getValueOrDefault(record.getCellManufacturerName(), ""));
        data.put("cellGSTInvoiceNo", getValueOrDefault(record.getCellGSTInvoiceNo(), ""));
        data.put("meterMake", getValueOrDefault(record.getMeterMake(), ""));
        data.put("meterNumber", getValueOrDefault(record.getMeterNumber(), ""));
        data.put("netMeterNumber", getValueOrDefault(record.getNetMeterNumber(), ""));
        data.put("invoiceNumber", getValueOrDefault(record.getInvoiceNumber(), ""));
        data.put("yearOfManufacturing", getValueOrDefault(record.getYearOfManufacturing(), ""));
        data.put("headerLogo", record.getHeaderLogo());
        data.put("sanctionedCapacity", getValueOrDefault(record.getSanctionedCapacity(), ""));
        data.put("installedCapacity", getValueOrDefault(record.getInstalledCapacity(), ""));
        data.put("installationDate", installationDate);
        data.put("moduleMake", getValueOrDefault(record.getModuleMake(), ""));
        data.put("wattagePerModule", getValueOrDefault(record.getWattagePerModule(), ""));
        data.put("numberOfModules", getValueOrDefault(record.getNumberOfModules(), ""));
        data.put("totalCapacityKWP", getValueOrDefault(record.getTotalCapacityKWP(), ""));
        data.put("moduleSerialNumbers", getValueOrDefault(record.getModuleSerialNumbers(), ""));
        data.put("numberOfStrings", getValueOrDefault(record.getNumberOfStrings(), ""));
        data.put("inverterMake", getValueOrDefault(record.getInverterMake(), ""));
        data.put("inverterModelNumber", getValueOrDefault(record.getInverterModelNumber(), ""));
        data.put("inverterCapacity", getValueOrDefault(record.getInverterCapacity(), ""));

        // Vendor & Witness Information
        data.put("vendorName", getValueOrDefault(record.getVendorName(), ""));
        data.put("vendorAddress", getValueOrDefault(record.getVendorAddress(), ""));
        data.put("vendorMobile", getValueOrDefault(record.getVendorMobile(), ""));
        data.put("vendorEmail", getValueOrDefault(record.getVendorEmail(), ""));
        data.put("authorizedPersonName", getValueOrDefault(record.getAuthorizedPersonName(), ""));
        data.put("msedclOfficerName", getValueOrDefault(record.getMsedclOfficerName(), ""));
        data.put("witness1Name", getValueOrDefault(record.getWitness1Name(), ""));
        data.put("witness1Address", getValueOrDefault(record.getWitness1Address(), ""));
        data.put("witness2Name", getValueOrDefault(record.getWitness2Name(), ""));
        data.put("witness2Address", getValueOrDefault(record.getWitness2Address(), ""));

        // Dates
        data.put("currentDate", now.format(dateFormatter));
        data.put("day", String.valueOf(now.getDayOfMonth()));
        data.put("month", now.format(monthFormatter));
        data.put("year", String.valueOf(now.getYear()));

        // File Uploads
        data.put("vendorSignature", record.getVendorSignature());
        data.put("consumerSignature", record.getConsumerSignature());
        data.put("msedclSignature", record.getMsedclSignature());
        data.put("witnessSignature", record.getWitnessSignature());
        data.put("netMeteringStamp", record.getNetMeteringStamp());
        data.put("annexureTwoStamp", record.getAnnexureTwoStamp());
        data.put("defaultArihantHeader", convertImageToBase64("/Arihant_Header.png"));
        data.put("defaultMsedclHeader", convertImageToBase64("/MSEDCL_Header.png"));
        data.put("cotationFirstPageImage", convertImageToBase64("/" + IMAGE_COTATION_1ST_PAGE));
        data.put("secondPageFirstImage", convertImageToBase64("/" + IMAGE_SECOND_PAGE_FIRST));
        data.put("secondPageSecondImage", convertImageToBase64("/" + IMAGE_SECOND_PAGE_SECOND));
        data.put("secondPageThirdImage", convertImageToBase64("/" + IMAGE_SECOND_PAGE_THIRD));
        data.put("secondPageFourthImage", convertImageToBase64("/" + IMAGE_SECOND_PAGE_FOURTH));

// DEBUG LOGS - Check header paths
        System.out.println("=== HEADER DEBUG ===");
        System.out.println("defaultArihantHeader: " + data.get("defaultArihantHeader"));
        System.out.println("defaultMsedclHeader: " + data.get("defaultMsedclHeader"));
        System.out.println("headerLogo from record: " + record.getHeaderLogo());
        System.out.println("=== END DEBUG ===");

        // Aadhar Images
        List<String> aadharImageUrls = new ArrayList<>();
        if (record.getAadharImages() != null && !record.getAadharImages().isEmpty()) {
            for (String imageUrl : record.getAadharImages()) {
                if (imageUrl != null && imageUrl.startsWith("http")) {
                    aadharImageUrls.add(imageUrl);
                }
            }
        }
        data.put("aadharImageUrls", aadharImageUrls);
        data.put("aadharImages", record.getAadharImages());

        // Site Photos
        List<String> processedSitePhotos = new ArrayList<>();
        if (record.getSitePhotos() != null) {
            for (String photo : record.getSitePhotos()) {
                if (photo != null) {
                    if (photo.startsWith("http")) {
                        processedSitePhotos.add(photo);
                    } else {
                        processedSitePhotos.add("/api/uploads/" + photo);
                    }
                }
            }
        }
        data.put("sitePhotos", processedSitePhotos);

        return data;
    }

    private String getPdfFilename(String type) {
        switch(type) {
            case "wcr": return "WCR_Undertaking_Guarantee_Aadhar.pdf";
            case "proforma-a": return "Annexure-I_Proforma-A.pdf";
            case "dcr": return "Declaration_FOR_DCR.pdf";
            case "agreement": return "NET_METERING_CONNECTION_AGREEMENT.pdf";
//            case "indemnity": return "INDEMNITY_BOND.pdf";
            case "SOLAR_PROPOSAL_11KW": return "11KW_Solar_Proposal.pdf";
            case "METER_TESTING_REQUEST": return "Meter_Testing_Request.pdf";
            case "PM_SURYA_GHAR_AGREEMENT": return "PM_Surya_Ghar_Agreement.pdf";
            case "FrontSix": return "Pre_Commissioning_Report_FrontSix.pdf";
            case "site-photos": return "Site_Photos.pdf";
            default: return "document_" + System.currentTimeMillis() + ".pdf";
        }
    }

    private String getValueOrDefault(Object value, String defaultValue) {
        if (value == null) return defaultValue;
        String stringValue = String.valueOf(value);
        return stringValue.isEmpty() ? defaultValue : stringValue;
    }

    private String convertImageToBase64(String imagePath) {
        try {
            org.springframework.core.io.Resource resource = new org.springframework.core.io.ClassPathResource("static" + imagePath);
            byte[] imageBytes = resource.getContentAsByteArray();
            String base64 = java.util.Base64.getEncoder().encodeToString(imageBytes);
            String mimeType = imagePath.endsWith(".png") ? "image/png" : "image/jpeg";
            return "data:" + mimeType + ";base64," + base64;
        } catch (Exception e) {
            System.err.println("Failed to load image: " + imagePath + " - " + e.getMessage());
            return "";
        }
    }

    private void checkDownloadPermission(String format) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Owner currentUser = ownerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (currentUser.getRole() == UserRole.SUPER_ADMIN) {
            return;
        }

        if (!currentUser.getPermissions().contains(Permission.DOWNLOAD)) {
            throw new RuntimeException("You don't have permission to download documents");
        }
    }
}