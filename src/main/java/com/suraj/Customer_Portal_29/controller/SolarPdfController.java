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

    private Owner getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ownerRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/{id}/{type}/word")
    public ResponseEntity<byte[]> downloadWord(@PathVariable String id, @PathVariable String type) {
        checkDownloadPermission("word");
        Map<String, Object> data = buildData(id, false);
        byte[] wordDoc = wordService.generateWord(type, data);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + type + ".doc")
                .contentType(MediaType.TEXT_HTML)
                .body(wordDoc);
    }

    @GetMapping("/{id}/{type}")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable String id, @PathVariable String type) {
        checkDownloadPermission("pdf");
        Map<String, Object> data = buildData(id, true);
        byte[] pdf = pdfService.generatePdfAsync(type, data).join();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + getPdfFilename(type))
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
            byte[] wordDoc = wordService.generateCombinedWord(buildData(id, true));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=All_In_One.doc")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(wordDoc);
        }
    }

    @GetMapping("/{id}/all-in-one/word")
    public ResponseEntity<byte[]> downloadAllInOneWord(@PathVariable String id) {
        checkDownloadPermission("word");
        Map<String, Object> data = buildData(id, true);
        byte[] wordDoc = wordService.generateCombinedWord(data);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=All_In_One.doc")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(wordDoc);
    }

    private Map<String, Object> buildData(String id, boolean includeCompleteData) {
        SolarRecordResponseDto record = solarService.findById(id);
        Owner currentUser = getCurrentUser();
        Map<String, Object> data = new HashMap<>();
        LocalDate now = LocalDate.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM");
        DateTimeFormatter installDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

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
        data.put("cellManufacturerName", getValueOrDefault(record.getCellManufacturerName(), ""));
        data.put("cellGSTInvoiceNo", getValueOrDefault(record.getCellGSTInvoiceNo(), ""));
        data.put("meterMake", getValueOrDefault(record.getMeterMake(), ""));
        data.put("meterNumber", getValueOrDefault(record.getMeterNumber(), ""));
        data.put("netMeterNumber", getValueOrDefault(record.getNetMeterNumber(), ""));
        data.put("invoiceNumber", getValueOrDefault(record.getInvoiceNumber(), ""));
        data.put("yearOfManufacturing", getValueOrDefault(record.getYearOfManufacturing(), ""));
        data.put("sanctionedCapacity", getValueOrDefault(record.getSanctionedCapacity(), ""));
        data.put("installedCapacity", getValueOrDefault(record.getInstalledCapacity(), ""));

        String installationDate = record.getInstallationDate();
        if (installationDate == null || installationDate.isEmpty()) {
            installationDate = now.format(installDateFormatter);
        } else {
            try {
                LocalDate parsedDate = LocalDate.parse(installationDate);
                installationDate = parsedDate.format(installDateFormatter);
            } catch (Exception e) {
                installationDate = now.format(installDateFormatter);
            }
        }
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
        data.put("totalAmountIncludingGST", getValueOrDefault(record.getTotalAmountIncludingGST(), ""));

        Double totalAmount = record.getTotalAmountIncludingGST();
        Double installedCap = record.getInstalledCapacity();
        String ratePerWatt = "", baseAmount = "", gstAmount = "";
        if (totalAmount != null && installedCap != null && installedCap > 0) {
            double calculatedRate = totalAmount / (installedCap * 1000);
            ratePerWatt = String.format("%.2f", calculatedRate);
            baseAmount = String.format("%.0f", totalAmount * 100 / 105);
            gstAmount = String.format("%.0f", totalAmount * 5 / 105);
        }
        data.put("ratePerWatt", ratePerWatt);
        data.put("baseAmount", baseAmount);
        data.put("gstAmount", gstAmount);

        data.put("vendorName", getValueOrDefault(currentUser.getName(), ""));
        data.put("vendorAddress", getValueOrDefault(currentUser.getVendorAddress(), ""));
        data.put("vendorMobile", getValueOrDefault(currentUser.getMobile(), ""));
        data.put("vendorEmail", getValueOrDefault(currentUser.getEmail(), ""));
        data.put("authorizedPersonName", getValueOrDefault(currentUser.getAuthorizedPersonName(), ""));
        data.put("designation", getValueOrDefault(currentUser.getDesignation(), "Proprietor"));

        data.put("witness1Name", getValueOrDefault(currentUser.getWitness1Name(), ""));
        data.put("witness1Address", getValueOrDefault(currentUser.getWitness1Address(), ""));
        data.put("witness2Name", getValueOrDefault(currentUser.getWitness2Name(), ""));
        data.put("witness2Address", getValueOrDefault(currentUser.getWitness2Address(), ""));

        data.put("bankAccountName", getValueOrDefault(currentUser.getBankAccountName(), ""));
        data.put("bankAccountNumber", getValueOrDefault(currentUser.getBankAccountNumber(), ""));
        data.put("bankName", getValueOrDefault(currentUser.getBankName(), ""));
        data.put("bankIfscCode", getValueOrDefault(currentUser.getBankIfscCode(), ""));
        data.put("branchName", getValueOrDefault(currentUser.getBranchName(), ""));

        String headerLogoUrl = getValueOrDefault(currentUser.getHeaderLogoUrl(), "");
        List<String> headerLogoList = new ArrayList<>();
        if (headerLogoUrl != null && !headerLogoUrl.isEmpty()) {
            headerLogoList.add(headerLogoUrl);
        }
        data.put("headerLogo", headerLogoList);

        String vendorSignatureUrl = getValueOrDefault(currentUser.getVendorSignatureUrl(), "");
        List<String> vendorSignatureList = new ArrayList<>();
        if (vendorSignatureUrl != null && !vendorSignatureUrl.isEmpty()) {
            vendorSignatureList.add(vendorSignatureUrl);
        }
        data.put("vendorSignature", vendorSignatureList);

        data.put("witness1Signature", getValueOrDefault(currentUser.getWitness1SignatureUrl(), ""));
        data.put("witness2Signature", getValueOrDefault(currentUser.getWitness2SignatureUrl(), ""));
        data.put("consumerSignature", record.getConsumerSignature() != null ? record.getConsumerSignature() : Collections.emptyList());
        data.put("netMeteringStamp", record.getNetMeteringStamp() != null ? record.getNetMeteringStamp() : Collections.emptyList());
        data.put("annexureTwoStamp", record.getAnnexureTwoStamp() != null ? record.getAnnexureTwoStamp() : Collections.emptyList());
        data.put("aadharImages", record.getAadharImages() != null ? record.getAadharImages() : Collections.emptyList());

        List<String> processedSitePhotos = new ArrayList<>();
        if (record.getSitePhotos() != null) {
            for (String photo : record.getSitePhotos()) {
                if (photo != null) {
                    String photoUrl = photo.startsWith("http") ? photo : "/api/uploads/" + photo;
                    processedSitePhotos.add(photoUrl);
                }
            }
        }
        data.put("sitePhotos", processedSitePhotos);

        data.put("currentDate", now.format(dateFormatter));
        data.put("day", String.valueOf(now.getDayOfMonth()));
        data.put("month", now.format(monthFormatter));
        data.put("year", String.valueOf(now.getYear()));

        data.put("defaultArihantHeader", convertImageToBase64("/Arihant_Header.png"));
        data.put("defaultMsedclHeader", convertImageToBase64("/MSEDCL_Header.png"));
        data.put("cotationFirstPageImage", convertImageToBase64("/CotationFirstPageImage.png"));
        data.put("secondPageFirstImage", convertImageToBase64("/SecondPageFirstImage.png"));
        data.put("secondPageSecondImage", convertImageToBase64("/SecondPageSecondImage.png"));
        data.put("secondPageThirdImage", convertImageToBase64("/SecondPageThirdImage.png"));
        data.put("secondPageFourthImage", convertImageToBase64("/SecondPageFourthImage.png"));

        return data;
    }

    private String getPdfFilename(String type) {
        Map<String, String> names = new HashMap<>();
        names.put("wcr", "WCR_Undertaking_Guarantee_Aadhar.pdf");
        names.put("proforma-a", "Annexure-I_Proforma-A.pdf");
        names.put("dcr", "Declaration_FOR_DCR.pdf");
        names.put("agreement", "NET_METERING_CONNECTION_AGREEMENT.pdf");
        names.put("SOLAR_PROPOSAL_11KW", "11KW_Solar_Proposal.pdf");
        names.put("METER_TESTING_REQUEST", "Meter_Testing_Request.pdf");
        names.put("PM_SURYA_GHAR_AGREEMENT", "PM_Surya_Ghar_Agreement.pdf");
        names.put("FrontSix", "Pre_Commissioning_Report_FrontSix.pdf");
        names.put("site-photos", "Site_Photos.pdf");
        return names.getOrDefault(type, "document_" + System.currentTimeMillis() + ".pdf");
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