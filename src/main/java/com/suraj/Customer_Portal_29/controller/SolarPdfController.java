package com.suraj.Customer_Portal_29.controller;

import com.suraj.Customer_Portal_29.dto.request.CommitDownloadRequestDto;
import com.suraj.Customer_Portal_29.dto.response.PreviewResponseDto;
import com.suraj.Customer_Portal_29.dto.response.SolarRecordResponseDto;
import com.suraj.Customer_Portal_29.entity.Owner;
import com.suraj.Customer_Portal_29.entity.PdfDocType;
import com.suraj.Customer_Portal_29.entity.Permission;
import com.suraj.Customer_Portal_29.entity.PreviewToken;
import com.suraj.Customer_Portal_29.entity.UserRole;
import com.suraj.Customer_Portal_29.repository.OwnerRepository;
import com.suraj.Customer_Portal_29.service.PdfGeneratorService;
import com.suraj.Customer_Portal_29.service.PdfMergerService;
import com.suraj.Customer_Portal_29.service.PreviewTokenStore;
import com.suraj.Customer_Portal_29.service.SolarRecordService;
import com.suraj.Customer_Portal_29.service.WalletService;
import com.suraj.Customer_Portal_29.service.WordGeneratorService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/solar/pdf")
public class SolarPdfController {

    private final SolarRecordService solarService;
    private final PdfGeneratorService pdfService;
    private final WordGeneratorService wordService;
    private final PdfMergerService pdfMergerService;
    private final OwnerRepository ownerRepository;
    private final PreviewTokenStore previewTokenStore;
    private final WalletService walletService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM");
    private static final DateTimeFormatter INSTALL_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final String[] ALL_NINE_TYPES = {
            "METER_TESTING_REQUEST", "FrontSix", "wcr", "proforma-a",
            "dcr", "agreement", "site-photos"
    };

    private static final Map<String, String> PDF_FILENAMES = Map.ofEntries(
            Map.entry("wcr", "WCR_Undertaking_Guarantee_Aadhar.pdf"),
            Map.entry("proforma-a", "Annexure-I_Proforma-A.pdf"),
            Map.entry("dcr", "Declaration_FOR_DCR.pdf"),
            Map.entry("agreement", "NET_METERING_CONNECTION_AGREEMENT.pdf"),
            Map.entry("SOLAR_PROPOSAL_11KW", "11KW_Solar_Proposal.pdf"),
            Map.entry("METER_TESTING_REQUEST", "Meter_Testing_Request.pdf"),
            Map.entry("PM_SURYA_GHAR_AGREEMENT", "PM_Surya_Ghar_Agreement.pdf"),
            Map.entry("FrontSix", "Pre_Commissioning_Report_FrontSix.pdf"),
            Map.entry("site-photos", "Site_Photos.pdf"),
            Map.entry("all-in-one", "All_In_One.pdf"),
            Map.entry("all-nine", "All_Nine.pdf")
    );

    public SolarPdfController(SolarRecordService solarService,
                              PdfGeneratorService pdfService,
                              WordGeneratorService wordService,
                              PdfMergerService pdfMergerService,
                              OwnerRepository ownerRepository,
                              PreviewTokenStore previewTokenStore,
                              WalletService walletService) {
        this.solarService = solarService;
        this.pdfService = pdfService;
        this.wordService = wordService;
        this.pdfMergerService = pdfMergerService;
        this.ownerRepository = ownerRepository;
        this.previewTokenStore = previewTokenStore;
        this.walletService = walletService;
    }

    private Owner getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ownerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private boolean isSuperAdmin(Owner user) {
        return user.getRole() == UserRole.SUPER_ADMIN;
    }

    private void chargeIfNeeded(Owner user, PdfDocType docType, String recordId, String previewToken) {
        if (isSuperAdmin(user)) return;

        BigDecimal cost = BigDecimal.valueOf(docType.getCostInRupees());
        walletService.debit(
                user.getId(),
                cost,
                docType,
                recordId,
                previewToken,
                user.getEmail()
        );

        BigDecimal newBalance = walletService.getMyWallet().getBalance();
        user.setWalletBalance(newBalance);
        ownerRepository.save(user);
    }

    private void checkDownloadPermission() {
        Owner currentUser = getCurrentUser();
        if (isSuperAdmin(currentUser)) return;
        if (!currentUser.getPermissions().contains(Permission.DOWNLOAD)) {
            throw new RuntimeException("You don't have permission to download documents");
        }
    }

    private String getPdfFilename(String type) {
        return PDF_FILENAMES.getOrDefault(type,
                "document_" + System.currentTimeMillis() + ".pdf");
    }

    private String getValueOrDefault(Object value, String defaultValue) {
        if (value == null) return defaultValue;
        String s = String.valueOf(value);
        return s.isEmpty() ? defaultValue : s;
    }

    private Double parseToDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            String s = String.valueOf(value).trim().replace(",", "");
            return s.isEmpty() ? null : Double.parseDouble(s);
        } catch (NumberFormatException e) {
            System.err.println("Failed to parse amount: '" + value + "'");
            return null;
        }
    }

    private String convertImageToBase64(String imagePath) {
        try {
            Resource resource = new ClassPathResource("static" + imagePath);
            byte[] bytes = resource.getContentAsByteArray();
            String mime = imagePath.endsWith(".png") ? "image/png" : "image/jpeg";
            return "data:" + mime + ";base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            return "";
        }
    }

    @GetMapping("/{id}/{type}/word")
    public ResponseEntity<byte[]> downloadWord(@PathVariable String id,
                                               @PathVariable String type,
                                               @RequestParam(defaultValue = "true") boolean includeSignatures) {
        checkDownloadPermission();
        Map<String, Object> data = buildData(id, false, includeSignatures);
        byte[] wordDoc = wordService.generateWord(type, data);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + type + ".doc")
                .contentType(MediaType.TEXT_HTML)
                .body(wordDoc);
    }

    @GetMapping("/{id}/{type}")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable String id,
                                              @PathVariable String type,
                                              @RequestParam(defaultValue = "true") boolean includeSignatures) {
        checkDownloadPermission();

        Owner currentUser = getCurrentUser();
        PdfDocType docType = PdfDocType.fromUrlSegment(type);
        if (docType == null) {
            throw new RuntimeException("Invalid doc type: " + type);
        }

        chargeIfNeeded(currentUser, docType, id, null);

        Map<String, Object> data = buildData(id, true, includeSignatures);
        byte[] pdf = pdfService.generatePdfAsync(type, data).join();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + getPdfFilename(type))
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/{id}/all-in-one")
    public ResponseEntity<byte[]> downloadAllInOnePdf(@PathVariable String id,
                                                      @RequestParam(defaultValue = "true") boolean includeSignatures) {
        checkDownloadPermission();

        Owner currentUser = getCurrentUser();
        chargeIfNeeded(currentUser, PdfDocType.ALL_IN_ONE, id, null);

        try {
            Map<String, Object> data = buildData(id, true, includeSignatures);
            List<byte[]> pdfs = new ArrayList<>();
            for (String type : ALL_NINE_TYPES) {
                try {
                    byte[] pdf = pdfService.generatePdf(type, data);
                    if (pdf != null && pdf.length > 500) pdfs.add(pdf);
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
            byte[] wordDoc = wordService.generateCombinedWord(buildData(id, true, includeSignatures));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=All_In_One.doc")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(wordDoc);
        }
    }

    @GetMapping("/{id}/all-in-one/word")
    public ResponseEntity<byte[]> downloadAllInOneWord(@PathVariable String id,
                                                       @RequestParam(defaultValue = "true") boolean includeSignatures) {
        checkDownloadPermission();
        Map<String, Object> data = buildData(id, true, includeSignatures);
        byte[] wordDoc = wordService.generateCombinedWord(data);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=All_In_One.doc")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(wordDoc);
    }

    @GetMapping("/{id}/{type}/preview")
    public PreviewResponseDto previewPdf(@PathVariable String id,
                                         @PathVariable String type,
                                         @RequestParam(defaultValue = "true") boolean includeSignatures) {

        PdfDocType docType = PdfDocType.fromUrlSegment(type);
        if (docType == null || docType == PdfDocType.ALL_NINE) {
            throw new RuntimeException("Invalid doc type for preview: " + type);
        }

        Owner currentUser = getCurrentUser();
        byte[] pdf = renderPdfBytes(docType, id, includeSignatures);
        PreviewToken token = previewTokenStore.create(currentUser.getEmail(), id, docType, pdf);

        int cost = isSuperAdmin(currentUser) ? 0 : docType.getCostInRupees();

        return new PreviewResponseDto(
                token.getToken(),
                docType.name(),
                docType.toUrlSegment(),
                cost,
                pdf.length,
                300,
                Base64.getEncoder().encodeToString(pdf)
        );
    }

    @GetMapping("/{id}/all-nine/preview")
    public PreviewResponseDto previewAllNine(@PathVariable String id,
                                             @RequestParam(defaultValue = "true") boolean includeSignatures) {

        Owner currentUser = getCurrentUser();
        byte[] pdf = renderPdfBytes(PdfDocType.ALL_NINE, id, includeSignatures);
        PreviewToken token = previewTokenStore.create(
                currentUser.getEmail(), id, PdfDocType.ALL_NINE, pdf);

        int cost = isSuperAdmin(currentUser) ? 0 : PdfDocType.ALL_NINE.getCostInRupees();

        return new PreviewResponseDto(
                token.getToken(),
                PdfDocType.ALL_NINE.name(),
                PdfDocType.ALL_NINE.toUrlSegment(),
                cost,
                pdf.length,
                300,
                Base64.getEncoder().encodeToString(pdf)
        );
    }

    @PostMapping("/commit-download")
    public ResponseEntity<byte[]> commitDownload(@RequestBody CommitDownloadRequestDto request) {

        if (request == null || request.getPreviewToken() == null || request.getPreviewToken().isBlank()) {
            throw new RuntimeException("previewToken is required");
        }

        Owner currentUser = getCurrentUser();
        PreviewToken pt = previewTokenStore.consume(request.getPreviewToken(), currentUser.getEmail());

        if (pt == null) {
            throw new RuntimeException("Preview expired or invalid. Please preview again.");
        }

        if (!isSuperAdmin(currentUser)) {
            chargeIfNeeded(currentUser, pt.getDocType(), pt.getSolarRecordId(), pt.getToken());
        }

        previewTokenStore.remove(pt.getToken());

        byte[] pdf = pt.getPdfBytes();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + getPdfFilename(pt.getDocType().toUrlSegment()))
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private byte[] renderPdfBytes(PdfDocType docType, String recordId, boolean includeSignatures) {
        if (docType == PdfDocType.ALL_NINE || docType == PdfDocType.ALL_IN_ONE) {
            Map<String, Object> data = buildData(recordId, true, includeSignatures);
            List<byte[]> pdfs = new ArrayList<>();
            for (String t : ALL_NINE_TYPES) {
                try {
                    byte[] b = pdfService.generatePdf(t, data);
                    if (b != null && b.length > 500) pdfs.add(b);
                } catch (Exception ignored) {
                }
            }
            try {
                return pdfMergerService.mergePdfs(pdfs);
            } catch (Exception e) {
                throw new RuntimeException(docType.name() + " generation failed: " + e.getMessage());
            }
        }

        Map<String, Object> data = buildData(recordId, true, includeSignatures);
        return pdfService.generatePdf(docType.toUrlSegment(), data);
    }

    private Map<String, Object> buildData(String id, boolean includeCompleteData, boolean includeSignatures) {
        SolarRecordResponseDto record = solarService.findById(id);
        Owner currentUser = getCurrentUser();

        Map<String, Object> data = new HashMap<>();
        LocalDate now = LocalDate.now();

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
            installationDate = now.format(INSTALL_DATE_FORMATTER);
        } else {
            try {
                LocalDate parsedDate = LocalDate.parse(installationDate);
                installationDate = parsedDate.format(INSTALL_DATE_FORMATTER);
            } catch (Exception e) {
                installationDate = now.format(INSTALL_DATE_FORMATTER);
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

        if (includeSignatures) {
            String vendorSigUrl = getValueOrDefault(currentUser.getVendorSignatureUrl(), "");
            List<String> vendorSigList = new ArrayList<>();
            if (vendorSigUrl != null && !vendorSigUrl.isEmpty()) {
                vendorSigList.add(vendorSigUrl);
            }
            data.put("vendorSignature", vendorSigList);

            String witness1SigUrl = getValueOrDefault(currentUser.getWitness1SignatureUrl(), "");
            List<String> witness1SigList = new ArrayList<>();
            if (witness1SigUrl != null && !witness1SigUrl.isEmpty()) {
                witness1SigList.add(witness1SigUrl);
            }
            data.put("witness1Signature", witness1SigList);

            String witness2SigUrl = getValueOrDefault(currentUser.getWitness2SignatureUrl(), "");
            List<String> witness2SigList = new ArrayList<>();
            if (witness2SigUrl != null && !witness2SigUrl.isEmpty()) {
                witness2SigList.add(witness2SigUrl);
            }
            data.put("witness2Signature", witness2SigList);

            data.put("consumerSignature",
                    record.getConsumerSignature() != null
                            ? record.getConsumerSignature()
                            : Collections.emptyList());
        } else {
            data.put("vendorSignature", Collections.emptyList());
            data.put("witness1Signature", Collections.emptyList());
            data.put("witness2Signature", Collections.emptyList());
            data.put("consumerSignature", Collections.emptyList());
        }

        data.put("netMeteringStamp",
                record.getNetMeteringStamp() != null ? record.getNetMeteringStamp() : Collections.emptyList());
        data.put("annexureTwoStamp",
                record.getAnnexureTwoStamp() != null ? record.getAnnexureTwoStamp() : Collections.emptyList());
        data.put("aadharImages",
                record.getAadharImages() != null ? record.getAadharImages() : Collections.emptyList());

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

        data.put("currentDate", now.format(DATE_FORMATTER));
        data.put("day", String.valueOf(now.getDayOfMonth()));
        data.put("month", now.format(MONTH_FORMATTER));
        data.put("year", String.valueOf(now.getYear()));

        data.put("defaultArihantHeader", convertImageToBase64("/Arihant_Header.jpeg"));
        data.put("defaultMsedclHeader", convertImageToBase64("/MSEDCL_Header.png"));
        data.put("cotationFirstPageImage", convertImageToBase64("/CotationFirstPageImage.png"));
        data.put("secondPageFirstImage", convertImageToBase64("/SecondPageFirstImage.png"));
        data.put("secondPageSecondImage", convertImageToBase64("/SecondPageSecondImage.png"));
        data.put("secondPageThirdImage", convertImageToBase64("/SecondPageThirdImage.png"));
        data.put("secondPageFourthImage", convertImageToBase64("/SecondPageFourthImage.png"));

        return data;
    }
}