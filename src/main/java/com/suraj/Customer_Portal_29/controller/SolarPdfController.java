package com.suraj.Customer_Portal_29.controller;

import com.suraj.Customer_Portal_29.dto.response.SolarRecordResponseDto;
import com.suraj.Customer_Portal_29.entity.Owner;
import com.suraj.Customer_Portal_29.entity.Permission;
import com.suraj.Customer_Portal_29.entity.UserRole;
import com.suraj.Customer_Portal_29.repository.OwnerRepository;
import com.suraj.Customer_Portal_29.service.PdfCacheService;
import com.suraj.Customer_Portal_29.service.PdfGeneratorService;
import com.suraj.Customer_Portal_29.service.PdfMergerService;
import com.suraj.Customer_Portal_29.service.SolarRecordService;
import com.suraj.Customer_Portal_29.service.WordGeneratorService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.suraj.Customer_Portal_29.config.CurrentUserContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/solar/pdf")
public class SolarPdfController {

    private static final String ALL_IN_ONE_KEY = "ALL_IN_ONE";
    private static final String CACHE_HEADER = "X-PDF-Cache";
    private static final String CACHE_HIT = "HIT";
    private static final String CACHE_MISS = "MISS";

    private static final String[] ALL_IN_ONE_TYPES = {
            "METER_TESTING_REQUEST",
            "FrontSix",
            "wcr",
            "proforma-a",
            "dcr",
            "agreement",
            "site-photos"
    };

    private final SolarRecordService solarService;
    private final PdfGeneratorService pdfService;
    private final WordGeneratorService wordService;
    private final PdfMergerService pdfMergerService;
    private final OwnerRepository ownerRepository;
    private final PdfCacheService pdfCacheService;

    public SolarPdfController(SolarRecordService solarService,
                              PdfGeneratorService pdfService,
                              WordGeneratorService wordService,
                              PdfMergerService pdfMergerService,
                              OwnerRepository ownerRepository,
                              PdfCacheService pdfCacheService) {
        this.solarService = solarService;
        this.pdfService = pdfService;
        this.wordService = wordService;
        this.pdfMergerService = pdfMergerService;
        this.ownerRepository = ownerRepository;
        this.pdfCacheService = pdfCacheService;
    }

    @GetMapping("/{id}/{type}/word")
    public ResponseEntity<byte[]> downloadWord(@PathVariable String id,
                                               @PathVariable String type,
                                               @RequestParam(defaultValue = "true") boolean includeSignatures) {
        checkDownloadPermission();

        byte[] cached = pdfCacheService.getWord(id, type, includeSignatures);
        if (cached != null) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + type + ".doc")
                    .header(CACHE_HEADER, CACHE_HIT)
                    .contentType(MediaType.TEXT_HTML)
                    .body(cached);
        }

        Map<String, Object> data = buildData(id, includeSignatures);
        byte[] wordDoc = wordService.generateWord(type, data);
        pdfCacheService.putWord(id, type, includeSignatures, wordDoc);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + type + ".doc")
                .header(CACHE_HEADER, CACHE_MISS)
                .contentType(MediaType.TEXT_HTML)
                .body(wordDoc);
    }

    @GetMapping("/{id}/{type}")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable String id,
                                              @PathVariable String type,
                                              @RequestParam(defaultValue = "true") boolean includeSignatures) {
        checkDownloadPermission();

        byte[] cached = pdfCacheService.getPdf(id, type, includeSignatures);
        if (cached != null) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + getPdfFilename(type))
                    .header(CACHE_HEADER, CACHE_HIT)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(cached);
        }

        Map<String, Object> data = buildData(id, includeSignatures);
        byte[] pdf = pdfService.generatePdf(type, data);
        pdfCacheService.putPdf(id, type, includeSignatures, pdf);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + getPdfFilename(type))
                .header(CACHE_HEADER, CACHE_MISS)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/{id}/all-in-one")
    public ResponseEntity<byte[]> downloadAllInOnePdf(@PathVariable String id,
                                                      @RequestParam(defaultValue = "true") boolean includeSignatures) {
        checkDownloadPermission();

        byte[] cachedBundle = pdfCacheService.getPdf(id, ALL_IN_ONE_KEY, includeSignatures);
        if (cachedBundle != null) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=All_In_One.pdf")
                    .header(CACHE_HEADER, CACHE_HIT)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(cachedBundle);
        }

        Map<String, Object> data = buildData(id, includeSignatures);

        List<byte[]> pdfs = new ArrayList<>();
        for (String type : ALL_IN_ONE_TYPES) {
            byte[] cachedPart = pdfCacheService.getPdf(id, type, includeSignatures);
            if (cachedPart != null) {
                pdfs.add(cachedPart);
                continue;
            }
            try {
                byte[] generated = pdfService.generatePdf(type, data);
                if (generated != null && generated.length > 500) {
                    pdfCacheService.putPdf(id, type, includeSignatures, generated);
                    pdfs.add(generated);
                }
            } catch (Exception e) {
                System.err.println("Failed to generate: " + type + " - " + e.getMessage());
            }
        }

        if (pdfs.isEmpty()) {
            byte[] wordDoc = wordService.generateCombinedWord(data);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=All_In_One.doc")
                    .header(CACHE_HEADER, CACHE_MISS)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(wordDoc);
        }

        byte[] merged;
        try {
            merged = pdfMergerService.mergePdfs(pdfs);
        } catch (Exception e) {
            merged = pdfs.get(0);
        }

        pdfCacheService.putPdf(id, ALL_IN_ONE_KEY, includeSignatures, merged);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=All_In_One.pdf")
                .header(CACHE_HEADER, CACHE_MISS)
                .contentType(MediaType.APPLICATION_PDF)
                .body(merged);
    }

    @GetMapping("/{id}/all-in-one/word")
    public ResponseEntity<byte[]> downloadAllInOneWord(@PathVariable String id,
                                                       @RequestParam(defaultValue = "true") boolean includeSignatures) {
        checkDownloadPermission();

        byte[] cached = pdfCacheService.getWord(id, ALL_IN_ONE_KEY, includeSignatures);
        if (cached != null) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=All_In_One.doc")
                    .header(CACHE_HEADER, CACHE_HIT)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(cached);
        }

        Map<String, Object> data = buildData(id, includeSignatures);
        byte[] wordDoc = wordService.generateCombinedWord(data);
        pdfCacheService.putWord(id, ALL_IN_ONE_KEY, includeSignatures, wordDoc);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=All_In_One.doc")
                .header(CACHE_HEADER, CACHE_MISS)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(wordDoc);
    }

    private Map<String, Object> buildData(String id, boolean includeSignatures) {
        Map<String, Object> cached = pdfCacheService.getData(id, includeSignatures);
        if (cached != null) {
            return cached;
        }

        SolarRecordResponseDto record = solarService.findById(id);
        Owner currentUser = getCurrentUser();

        Map<String, Object> data = new HashMap<>();
        LocalDate now = LocalDate.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM");
        DateTimeFormatter installDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        data.put("name", safe(record.getName()));
        data.put("consumerNumber", safe(record.getConsumerNumber()));
        data.put("consumerName", safe(record.getName()));
        data.put("mobileNumber", safe(record.getMobileNumber()));
        data.put("email", safe(record.getEmail()));
        data.put("siteAddress", safe(record.getSiteAddress()));
        data.put("aadharNumber", safe(record.getAadharNumber()));
        data.put("place", safe(record.getPlace()));
        data.put("applicationNumber", safe(record.getApplicationNumber()));
        data.put("applicationDate", safe(record.getApplicationDate()));
        data.put("sanctionNumber", safe(record.getSanctionNumber()));
        data.put("billingUnit", safe(record.getBillingUnit()));
        data.put("connectionType", safe(record.getConnectionType()));
        data.put("cellManufacturerName", safe(record.getCellManufacturerName()));
        data.put("cellGSTInvoiceNo", safe(record.getCellGSTInvoiceNo()));
        data.put("meterMake", safe(record.getMeterMake()));
        data.put("meterNumber", safe(record.getMeterNumber()));
        data.put("netMeterNumber", safe(record.getNetMeterNumber()));
        data.put("invoiceNumber", safe(record.getInvoiceNumber()));
        data.put("yearOfManufacturing", safe(record.getYearOfManufacturing()));
        data.put("sanctionedCapacity", safe(record.getSanctionedCapacity()));
        data.put("installedCapacity", safe(record.getInstalledCapacity()));

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

        data.put("moduleMake", safe(record.getModuleMake()));
        data.put("wattagePerModule", safe(record.getWattagePerModule()));
        data.put("numberOfModules", safe(record.getNumberOfModules()));
        data.put("totalCapacityKWP", safe(record.getTotalCapacityKWP()));
        data.put("moduleSerialNumbers", safe(record.getModuleSerialNumbers()));
        data.put("numberOfStrings", safe(record.getNumberOfStrings()));
        data.put("inverterMake", safe(record.getInverterMake()));
        data.put("inverterModelNumber", safe(record.getInverterModelNumber()));
        data.put("inverterCapacity", safe(record.getInverterCapacity()));
        data.put("totalAmountIncludingGST", safe(record.getTotalAmountIncludingGST()));

        Double totalAmount = parseToDouble(record.getTotalAmountIncludingGST());
        Double installedCap = parseToDouble(record.getInstalledCapacity());
        String ratePerWatt = "";
        String ratePerWattDisplay = "";
        String baseAmount = "";
        String gstAmount = "";
        String cleaningCharge = "4500";
        String grossTotal = "";

        if (totalAmount != null && totalAmount > 0) {
            baseAmount = String.format("%.0f", totalAmount * 100 / 105);
            gstAmount = String.format("%.0f", totalAmount * 5 / 105);
            grossTotal = String.format("%.0f", totalAmount + 4500);

            if (installedCap != null && installedCap > 0) {
                double calculatedRate = totalAmount / (installedCap * 1000);
                ratePerWatt = String.format("%.2f", calculatedRate);
                ratePerWattDisplay = "Rs " + ratePerWatt + "/Watt";
            }
        }

        data.put("ratePerWatt", ratePerWatt);
        data.put("ratePerWattDisplay", ratePerWattDisplay);
        data.put("baseAmount", baseAmount);
        data.put("gstAmount", gstAmount);
        data.put("cleaningCharge", cleaningCharge);
        data.put("grossTotal", grossTotal);

        data.put("vendorName", safe(currentUser.getName()));
        data.put("vendorAddress", safe(currentUser.getVendorAddress()));
        data.put("vendorMobile", safe(currentUser.getMobile()));
        data.put("vendorEmail", safe(currentUser.getEmail()));
        data.put("authorizedPersonName", safe(currentUser.getAuthorizedPersonName()));
        data.put("designation", currentUser.getDesignation() != null && !currentUser.getDesignation().isEmpty()
                ? currentUser.getDesignation() : "Proprietor");

        data.put("witness1Name", safe(currentUser.getWitness1Name()));
        data.put("witness1Address", safe(currentUser.getWitness1Address()));
        data.put("witness2Name", safe(currentUser.getWitness2Name()));
        data.put("witness2Address", safe(currentUser.getWitness2Address()));

        data.put("bankAccountName", safe(currentUser.getBankAccountName()));
        data.put("bankAccountNumber", safe(currentUser.getBankAccountNumber()));
        data.put("bankName", safe(currentUser.getBankName()));
        data.put("bankIfscCode", safe(currentUser.getBankIfscCode()));
        data.put("branchName", safe(currentUser.getBranchName()));

        List<String> headerLogoList = new ArrayList<>();
        String headerLogoUrl = currentUser.getHeaderLogoUrl();
        if (headerLogoUrl != null && !headerLogoUrl.isEmpty()) {
            headerLogoList.add(headerLogoUrl);
        }
        data.put("headerLogo", headerLogoList);

        if (includeSignatures) {
            data.put("vendorSignature", singletonIfPresent(currentUser.getVendorSignatureUrl()));
            data.put("witness1Signature", singletonIfPresent(currentUser.getWitness1SignatureUrl()));
            data.put("witness2Signature", singletonIfPresent(currentUser.getWitness2SignatureUrl()));
            data.put("consumerSignature", record.getConsumerSignature() != null
                    ? record.getConsumerSignature() : Collections.emptyList());
        } else {
            data.put("vendorSignature", Collections.emptyList());
            data.put("witness1Signature", Collections.emptyList());
            data.put("witness2Signature", Collections.emptyList());
            data.put("consumerSignature", Collections.emptyList());
        }

        data.put("netMeteringStamp", record.getNetMeteringStamp() != null
                ? record.getNetMeteringStamp() : Collections.emptyList());
        data.put("annexureTwoStamp", record.getAnnexureTwoStamp() != null
                ? record.getAnnexureTwoStamp() : Collections.emptyList());
        data.put("aadharImages", record.getAadharImages() != null
                ? record.getAadharImages() : Collections.emptyList());

        List<String> processedSitePhotos = new ArrayList<>();
        if (record.getSitePhotos() != null) {
            for (String photo : record.getSitePhotos()) {
                if (photo != null) {
                    processedSitePhotos.add(photo.startsWith("http") ? photo : "/api/uploads/" + photo);
                }
            }
        }
        data.put("sitePhotos", processedSitePhotos);

        data.put("currentDate", now.format(dateFormatter));
        data.put("day", String.valueOf(now.getDayOfMonth()));
        data.put("month", now.format(monthFormatter));
        data.put("year", String.valueOf(now.getYear()));

        data.put("defaultArihantHeader", convertImageToBase64("/Arihant_Header.jpeg"));
        data.put("defaultMsedclHeader", convertImageToBase64("/MSEDCL_Header.png"));
        data.put("cotationFirstPageImage", convertImageToBase64("/CotationFirstPageImage.png"));
        data.put("secondPageFirstImage", convertImageToBase64("/SecondPageFirstImage.png"));
        data.put("secondPageSecondImage", convertImageToBase64("/SecondPageSecondImage.png"));
        data.put("secondPageThirdImage", convertImageToBase64("/SecondPageThirdImage.png"));
        data.put("secondPageFourthImage", convertImageToBase64("/SecondPageFourthImage.png"));

        pdfCacheService.putData(id, includeSignatures, data);
        return data;
    }

    private Owner getCurrentUser() {
        Owner cached = CurrentUserContext.get();
        if (cached != null) return cached;
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Owner user = ownerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        CurrentUserContext.set(user);
        return user;
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

    private String safe(Object value) {
        if (value == null) return "";
        String s = String.valueOf(value);
        return s.isEmpty() ? "" : s;
    }

    private List<String> singletonIfPresent(String url) {
        List<String> list = new ArrayList<>();
        if (url != null && !url.isEmpty()) {
            list.add(url);
        }
        return list;
    }

    private String convertImageToBase64(String imagePath) {
        try {
            org.springframework.core.io.Resource resource =
                    new org.springframework.core.io.ClassPathResource("static" + imagePath);
            byte[] imageBytes = resource.getContentAsByteArray();
            String base64 = java.util.Base64.getEncoder().encodeToString(imageBytes);
            String mimeType = imagePath.endsWith(".png") ? "image/png" : "image/jpeg";
            return "data:" + mimeType + ";base64," + base64;
        } catch (Exception e) {
            return "";
        }
    }

    private Double parseToDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Double) return (Double) value;
        if (value instanceof Integer) return ((Integer) value).doubleValue();
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            String s = String.valueOf(value).trim().replace(",", "");
            if (s.isEmpty()) return null;
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void checkDownloadPermission() {
        Owner currentUser = getCurrentUser();
        if (currentUser.getRole() == UserRole.SUPER_ADMIN) return;
        if (!currentUser.getPermissions().contains(Permission.DOWNLOAD)) {
            throw new RuntimeException("You don't have permission to download documents");
        }
    }
}