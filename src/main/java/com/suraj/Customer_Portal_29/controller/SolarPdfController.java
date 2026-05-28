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
        System.out.println("=== DEBUG: Getting Current User ===");
        System.out.println("Email from SecurityContext: " + email);
        Owner user = ownerRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        System.out.println("User found: " + user.getName() + " (ID: " + user.getId() + ")");
        System.out.println("User Role: " + user.getRole());
        System.out.println("User Vendor Name: " + user.getName());
        System.out.println("User Vendor Address: " + user.getVendorAddress());
        System.out.println("User Vendor Signature URL: " + user.getVendorSignatureUrl());
        System.out.println("User Witness1 Name: " + user.getWitness1Name());
        System.out.println("User Witness1 Signature URL: " + user.getWitness1SignatureUrl());
        System.out.println("User Witness2 Name: " + user.getWitness2Name());
        System.out.println("User Witness2 Signature URL: " + user.getWitness2SignatureUrl());
        System.out.println("User Header Logo URL: " + user.getHeaderLogoUrl());
        System.out.println("=== END User Debug ===");
        return user;
    }

    @GetMapping("/{id}/{type}/word")
    public ResponseEntity<byte[]> downloadWord(@PathVariable String id, @PathVariable String type) {
        System.out.println("=== WORD DOWNLOAD REQUEST ===");
        System.out.println("Record ID: " + id);
        System.out.println("Type: " + type);
        checkDownloadPermission("word");
        Map<String, Object> data = buildData(id, false);
        System.out.println("Data map size before Word generation: " + data.size());
        System.out.println("Key fields in data: " + data.keySet());
        byte[] wordDoc = wordService.generateWord(type, data);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + type + ".doc")
                .contentType(MediaType.TEXT_HTML)
                .body(wordDoc);
    }

    @GetMapping("/{id}/{type}")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable String id, @PathVariable String type) {
        System.out.println("=== PDF DOWNLOAD REQUEST ===");
        System.out.println("Record ID: " + id);
        System.out.println("Type: " + type);
        checkDownloadPermission("pdf");
        Map<String, Object> data = buildData(id, true);
        System.out.println("Data map size before PDF generation: " + data.size());
        System.out.println("Key fields in data: " + data.keySet());
        byte[] pdf = pdfService.generatePdfAsync(type, data).join();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + getPdfFilename(type))
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/{id}/all-in-one")
    public ResponseEntity<byte[]> downloadAllInOnePdf(@PathVariable String id) {
        System.out.println("=== ALL IN ONE PDF DOWNLOAD REQUEST ===");
        System.out.println("Record ID: " + id);
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
                        System.out.println("Generated PDF for: " + type + ", size: " + pdf.length);
                    } else {
                        System.out.println("Skipped PDF for: " + type + " (size too small or null)");
                    }
                } catch (Exception e) {
                    System.err.println("Failed: " + type + " - " + e.getMessage());
                }
            }
            if (!pdfs.isEmpty()) {
                byte[] mergedPdf = pdfMergerService.mergePdfs(pdfs);
                System.out.println("Merged PDF size: " + mergedPdf.length);
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
            System.err.println("All in one PDF failed: " + e.getMessage());
            e.printStackTrace();
            byte[] wordDoc = wordService.generateCombinedWord(buildData(id, true));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=All_In_One.doc")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(wordDoc);
        }
    }

    @GetMapping("/{id}/all-in-one/word")
    public ResponseEntity<byte[]> downloadAllInOneWord(@PathVariable String id) {
        System.out.println("=== ALL IN ONE WORD DOWNLOAD REQUEST ===");
        System.out.println("Record ID: " + id);
        checkDownloadPermission("word");
        Map<String, Object> data = buildData(id, true);
        byte[] wordDoc = wordService.generateCombinedWord(data);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=All_In_One.doc")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(wordDoc);
    }

    private Map<String, Object> buildData(String id, boolean includeCompleteData) {
        System.out.println("\n========== BUILD DATA START ==========");
        System.out.println("Record ID: " + id);
        System.out.println("Include Complete Data: " + includeCompleteData);

        // Fetch Solar Record
        SolarRecordResponseDto record = solarService.findById(id);
        System.out.println("\n--- SOLAR RECORD DATA ---");
        System.out.println("Consumer Name: " + record.getName());
        System.out.println("Consumer Number: " + record.getConsumerNumber());
        System.out.println("Mobile: " + record.getMobileNumber());
        System.out.println("Email: " + record.getEmail());
        System.out.println("Site Address: " + record.getSiteAddress());
        System.out.println("Place: " + record.getPlace());
        System.out.println("Installed Capacity: " + record.getInstalledCapacity());
        System.out.println("Consumer Signature: " + (record.getConsumerSignature() != null ? record.getConsumerSignature().size() + " file(s)" : "null"));

        // Fetch Current Logged-in User (Vendor)
        Owner currentUser = getCurrentUser();

        Map<String, Object> data = new HashMap<>();
        LocalDate now = LocalDate.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM");
        DateTimeFormatter installDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        // ============================================
        // 1. CONSUMER DETAILS (from SolarRecord)
        // ============================================
        System.out.println("\n--- ADDING CONSUMER DETAILS ---");
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

        // Calculate rate per watt
        Double totalAmount = record.getTotalAmountIncludingGST();
        Double installedCap = record.getInstalledCapacity();
        String ratePerWatt = "";
        String baseAmount = "";
        String gstAmount = "";
        if (totalAmount != null && installedCap != null && installedCap > 0) {
            double calculatedRate = totalAmount / (installedCap * 1000);
            ratePerWatt = String.format("%.2f", calculatedRate);
            baseAmount = String.format("%.0f", totalAmount * 100 / 105);
            gstAmount = String.format("%.0f", totalAmount * 5 / 105);
        }
        data.put("ratePerWatt", ratePerWatt);
        data.put("baseAmount", baseAmount);
        data.put("gstAmount", gstAmount);

        // ============================================
        // 2. VENDOR DETAILS (from Logged-in User Profile)
        // ============================================
        System.out.println("\n--- ADDING VENDOR DETAILS FROM PROFILE ---");

        String vendorName = getValueOrDefault(currentUser.getName(), "");
        String vendorAddress = getValueOrDefault(currentUser.getVendorAddress(), "");
        String vendorMobile = getValueOrDefault(currentUser.getVendorMobile(), "");
        String vendorEmail = getValueOrDefault(currentUser.getVendorEmail(), "");
        String authorizedPersonName = getValueOrDefault(currentUser.getAuthorizedPersonName(), "");
        String designation = getValueOrDefault(currentUser.getDesignation(), "Proprietor");

        System.out.println("Vendor Name from Profile: '" + vendorName + "'");
        System.out.println("Vendor Address from Profile: '" + vendorAddress + "'");
        System.out.println("Vendor Mobile from Profile: '" + vendorMobile + "'");
        System.out.println("Vendor Email from Profile: '" + vendorEmail + "'");
        System.out.println("Authorized Person Name: '" + authorizedPersonName + "'");
        System.out.println("Designation: '" + designation + "'");

        data.put("vendorName", vendorName);
        data.put("vendorAddress", vendorAddress);
        data.put("vendorMobile", vendorMobile);
        data.put("vendorEmail", vendorEmail);
        data.put("authorizedPersonName", authorizedPersonName);
        data.put("designation", designation);

        // ============================================
        // 3. WITNESS DETAILS (from Logged-in User Profile)
        // ============================================
        System.out.println("\n--- ADDING WITNESS DETAILS FROM PROFILE ---");

        String witness1Name = getValueOrDefault(currentUser.getWitness1Name(), "");
        String witness1Address = getValueOrDefault(currentUser.getWitness1Address(), "");
        String witness2Name = getValueOrDefault(currentUser.getWitness2Name(), "");
        String witness2Address = getValueOrDefault(currentUser.getWitness2Address(), "");

        System.out.println("Witness 1 Name: '" + witness1Name + "'");
        System.out.println("Witness 1 Address: '" + witness1Address + "'");
        System.out.println("Witness 2 Name: '" + witness2Name + "'");
        System.out.println("Witness 2 Address: '" + witness2Address + "'");

        data.put("witness1Name", witness1Name);
        data.put("witness1Address", witness1Address);
        data.put("witness2Name", witness2Name);
        data.put("witness2Address", witness2Address);

        // ============================================
        // 4. BANK DETAILS (from Logged-in User Profile)
        // ============================================
        System.out.println("\n--- ADDING BANK DETAILS FROM PROFILE ---");

        String bankAccountName = getValueOrDefault(currentUser.getBankAccountName(), "");
        String bankAccountNumber = getValueOrDefault(currentUser.getBankAccountNumber(), "");
        String bankName = getValueOrDefault(currentUser.getBankName(), "");
        String bankIfscCode = getValueOrDefault(currentUser.getBankIfscCode(), "");
        String branchName = getValueOrDefault(currentUser.getBranchName(), "");

        System.out.println("Bank Account Name: '" + bankAccountName + "'");
        System.out.println("Bank Account Number: '" + bankAccountNumber + "'");
        System.out.println("Bank Name: '" + bankName + "'");
        System.out.println("IFSC Code: '" + bankIfscCode + "'");
        System.out.println("Branch Name: '" + branchName + "'");

        data.put("bankAccountName", bankAccountName);
        data.put("bankAccountNumber", bankAccountNumber);
        data.put("bankName", bankName);
        data.put("bankIfscCode", bankIfscCode);
        data.put("branchName", branchName);

        // ============================================
        // 5. IMAGES & SIGNATURES (from Profile)
        // ============================================
        System.out.println("\n--- ADDING IMAGES & SIGNATURES FROM PROFILE ---");

        // Header Logo
        String headerLogoUrl = getValueOrDefault(currentUser.getHeaderLogoUrl(), "");
        List<String> headerLogoList = new ArrayList<>();
        if (headerLogoUrl != null && !headerLogoUrl.isEmpty()) {
            headerLogoList.add(headerLogoUrl);
            System.out.println("Header Logo URL: '" + headerLogoUrl + "'");
        } else {
            System.out.println("Header Logo URL: EMPTY - using default");
        }
        data.put("headerLogo", headerLogoList);

        // Vendor Signature
        String vendorSignatureUrl = getValueOrDefault(currentUser.getVendorSignatureUrl(), "");
        List<String> vendorSignatureList = new ArrayList<>();
        if (vendorSignatureUrl != null && !vendorSignatureUrl.isEmpty()) {
            vendorSignatureList.add(vendorSignatureUrl);
            System.out.println("Vendor Signature URL: '" + vendorSignatureUrl + "'");
        } else {
            System.out.println("Vendor Signature URL: EMPTY");
        }
        data.put("vendorSignature", vendorSignatureList);

        // Witness 1 Signature
        String witness1SignatureUrl = getValueOrDefault(currentUser.getWitness1SignatureUrl(), "");
        System.out.println("Witness 1 Signature URL: '" + witness1SignatureUrl + "'");
        data.put("witness1Signature", witness1SignatureUrl);

        // Witness 2 Signature
        String witness2SignatureUrl = getValueOrDefault(currentUser.getWitness2SignatureUrl(), "");
        System.out.println("Witness 2 Signature URL: '" + witness2SignatureUrl + "'");
        data.put("witness2Signature", witness2SignatureUrl);

        // Consumer Signature from SolarRecord
        List<String> consumerSignatureList = record.getConsumerSignature();
        if (consumerSignatureList != null && !consumerSignatureList.isEmpty()) {
            System.out.println("Consumer Signature URLs: " + consumerSignatureList.size() + " file(s)");
            for (int i = 0; i < consumerSignatureList.size(); i++) {
                System.out.println("  Consumer Signature [" + i + "]: " + consumerSignatureList.get(i));
            }
        } else {
            System.out.println("Consumer Signature: EMPTY");
        }
        data.put("consumerSignature", consumerSignatureList != null ? consumerSignatureList : Collections.emptyList());

        // ============================================
        // 6. STAMPS (from SolarRecord)
        // ============================================
        System.out.println("\n--- ADDING STAMPS FROM SOLAR RECORD ---");

        List<String> netMeteringStampList = record.getNetMeteringStamp();
        System.out.println("Net Metering Stamp: " + (netMeteringStampList != null && !netMeteringStampList.isEmpty() ? netMeteringStampList.get(0) : "EMPTY"));
        data.put("netMeteringStamp", netMeteringStampList != null ? netMeteringStampList : Collections.emptyList());

        List<String> annexureTwoStampList = record.getAnnexureTwoStamp();
        System.out.println("Annexure II Stamp: " + (annexureTwoStampList != null && !annexureTwoStampList.isEmpty() ? annexureTwoStampList.get(0) : "EMPTY"));
        data.put("annexureTwoStamp", annexureTwoStampList != null ? annexureTwoStampList : Collections.emptyList());

        // ============================================
        // 7. AADHAR IMAGES (from SolarRecord)
        // ============================================
        System.out.println("\n--- ADDING AADHAR IMAGES FROM SOLAR RECORD ---");

        List<String> aadharImageUrls = new ArrayList<>();
        if (record.getAadharImages() != null && !record.getAadharImages().isEmpty()) {
            System.out.println("Aadhar Images count: " + record.getAadharImages().size());
            for (String imageUrl : record.getAadharImages()) {
                if (imageUrl != null) {
                    aadharImageUrls.add(imageUrl);
                    System.out.println("  Aadhar Image: " + imageUrl);
                }
            }
        } else {
            System.out.println("Aadhar Images: EMPTY");
        }
        data.put("aadharImageUrls", aadharImageUrls);
        data.put("aadharImages", record.getAadharImages() != null ? record.getAadharImages() : Collections.emptyList());

        // ============================================
        // 8. SITE PHOTOS (from SolarRecord)
        // ============================================
        System.out.println("\n--- ADDING SITE PHOTOS FROM SOLAR RECORD ---");

        List<String> processedSitePhotos = new ArrayList<>();
        if (record.getSitePhotos() != null) {
            System.out.println("Site Photos count: " + record.getSitePhotos().size());
            for (String photo : record.getSitePhotos()) {
                if (photo != null) {
                    String photoUrl = photo.startsWith("http") ? photo : "/api/uploads/" + photo;
                    processedSitePhotos.add(photoUrl);
                    System.out.println("  Site Photo: " + photoUrl);
                }
            }
        } else {
            System.out.println("Site Photos: EMPTY");
        }
        data.put("sitePhotos", processedSitePhotos);

        // ============================================
        // 9. DATES
        // ============================================
        System.out.println("\n--- ADDING DATES ---");

        String currentDate = now.format(dateFormatter);
        String day = String.valueOf(now.getDayOfMonth());
        String month = now.format(monthFormatter);
        String year = String.valueOf(now.getYear());

        System.out.println("Current Date: " + currentDate);
        System.out.println("Day: " + day);
        System.out.println("Month: " + month);
        System.out.println("Year: " + year);

        data.put("currentDate", currentDate);
        data.put("day", day);
        data.put("month", month);
        data.put("year", year);

        // ============================================
        // 10. DEFAULT IMAGES (Base64 encoded)
        // ============================================
        System.out.println("\n--- ADDING DEFAULT IMAGES ---");

        String defaultArihantHeader = convertImageToBase64("/Arihant_Header.png");
        String defaultMsedclHeader = convertImageToBase64("/MSEDCL_Header.png");
        String cotationFirstPageImage = convertImageToBase64("/CotationFirstPageImage.png");
        String secondPageFirstImage = convertImageToBase64("/SecondPageFirstImage.png");
        String secondPageSecondImage = convertImageToBase64("/SecondPageSecondImage.png");
        String secondPageThirdImage = convertImageToBase64("/SecondPageThirdImage.png");
        String secondPageFourthImage = convertImageToBase64("/SecondPageFourthImage.png");

        System.out.println("Default Arihant Header: " + (defaultArihantHeader != null && !defaultArihantHeader.isEmpty() ? "LOADED" : "NOT LOADED"));
        System.out.println("Default MSEDCL Header: " + (defaultMsedclHeader != null && !defaultMsedclHeader.isEmpty() ? "LOADED" : "NOT LOADED"));
        System.out.println("Cotation Images: " + (cotationFirstPageImage != null && !cotationFirstPageImage.isEmpty() ? "LOADED" : "NOT LOADED"));

        data.put("defaultArihantHeader", defaultArihantHeader);
        data.put("defaultMsedclHeader", defaultMsedclHeader);
        data.put("cotationFirstPageImage", cotationFirstPageImage);
        data.put("secondPageFirstImage", secondPageFirstImage);
        data.put("secondPageSecondImage", secondPageSecondImage);
        data.put("secondPageThirdImage", secondPageThirdImage);
        data.put("secondPageFourthImage", secondPageFourthImage);

        // ============================================
        // 11. FINAL DATA MAP SUMMARY
        // ============================================
        System.out.println("\n========== BUILD DATA SUMMARY ==========");
        System.out.println("Total fields in data map: " + data.size());
        System.out.println("\n--- CRITICAL FIELDS CHECK ---");
        System.out.println("vendorName: '" + data.get("vendorName") + "'");
        System.out.println("vendorAddress: '" + data.get("vendorAddress") + "'");
        System.out.println("vendorSignature: " + (data.get("vendorSignature") != null ? ((List<?>)data.get("vendorSignature")).size() + " item(s)" : "null"));
        System.out.println("witness1Signature: '" + data.get("witness1Signature") + "'");
        System.out.println("witness2Signature: '" + data.get("witness2Signature") + "'");
        System.out.println("witness1Name: '" + data.get("witness1Name") + "'");
        System.out.println("witness2Name: '" + data.get("witness2Name") + "'");
        System.out.println("headerLogo: " + (data.get("headerLogo") != null ? ((List<?>)data.get("headerLogo")).size() + " item(s)" : "null"));
        System.out.println("consumerSignature: " + (data.get("consumerSignature") != null ? ((List<?>)data.get("consumerSignature")).size() + " item(s)" : "null"));
        System.out.println("========== BUILD DATA END ==========\n");

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
            System.err.println("Failed to load image: " + imagePath + " - " + e.getMessage());
            return "";
        }
    }

    private void checkDownloadPermission(String format) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Owner currentUser = ownerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (currentUser.getRole() == UserRole.SUPER_ADMIN) {
            System.out.println("Permission check: SUPER_ADMIN - access granted");
            return;
        }

        if (!currentUser.getPermissions().contains(Permission.DOWNLOAD)) {
            System.out.println("Permission check: User does not have DOWNLOAD permission - access denied");
            throw new RuntimeException("You don't have permission to download documents");
        }
        System.out.println("Permission check: DOWNLOAD permission granted");
    }
}