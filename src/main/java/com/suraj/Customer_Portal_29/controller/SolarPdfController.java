package com.suraj.Customer_Portal_29.controller;

import com.suraj.Customer_Portal_29.dto.response.SolarRecordResponseDto;
import com.suraj.Customer_Portal_29.service.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/solar/pdf")
public class SolarPdfController {

    private final SolarRecordService solarService;
    private final PdfGeneratorService pdfService;
    private final WordGeneratorService wordService;
    private final PdfMergerService pdfMergerService;

    public SolarPdfController(SolarRecordService solarService,
                              PdfGeneratorService pdfService,
                              WordGeneratorService wordService,
                              PdfMergerService pdfMergerService) {
        this.solarService = solarService;
        this.pdfService = pdfService;
        this.wordService = wordService;
        this.pdfMergerService = pdfMergerService;
    }

    @GetMapping("/{id}/{type}/word")
    public ResponseEntity<byte[]> downloadWord(@PathVariable String id, @PathVariable String type) {
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
        try {
            Map<String, Object> data = buildData(id, true);
            List<byte[]> pdfs = new ArrayList<>();
            String[] pdfTypes = {"FrontSix", "wcr", "proforma-a", "dcr", "agreement", "site-photos"};

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
        String installationDate = getValueOrDefault(record.getInstallationDate(),
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));

        data.put("name", getValueOrDefault(record.getName(), "_________________________"));
        data.put("consumerNumber", getValueOrDefault(record.getConsumerNumber(), "_________________________"));
        data.put("consumerName", getValueOrDefault(record.getName(), "_________________________"));
        data.put("siteAddress", getValueOrDefault(record.getSiteAddress(), "_________________________________________________________________________________"));
        data.put("category", getValueOrDefault(record.getCategory(), "Private Sector"));
        data.put("sanctionNumber", getValueOrDefault(record.getSanctionNumber(), "_________________________"));
        data.put("sanctionedCapacity", getValueOrDefault(record.getSanctionedCapacity(), "_________________________"));
        data.put("installedCapacity", getValueOrDefault(record.getInstalledCapacity(), "_________________________"));
        data.put("installationDate", installationDate);
        data.put("moduleMake", getValueOrDefault(record.getModuleMake(), "_________________________"));
        data.put("almmModelNumber", getValueOrDefault(record.getAlmmModelNumber(), "_________________________"));
        data.put("wattagePerModule", getValueOrDefault(record.getWattagePerModule(), "_________________________"));
        data.put("numberOfModules", getValueOrDefault(record.getNumberOfModules(), "_________________________"));
        data.put("totalCapacityKWP", getValueOrDefault(record.getTotalCapacityKWP(), "_________________________"));
        data.put("inverterRating", getValueOrDefault(record.getInverterRating(), "_________________________"));
        data.put("chargeControllerType", getValueOrDefault(record.getChargeControllerType(), "MPPT"));
        data.put("inverterCapacity", getValueOrDefault(record.getInverterCapacity(), "_________________________"));
        data.put("hpd", getValueOrDefault(record.getHpd(), "Yes"));
        data.put("yearOfManufacturing", getValueOrDefault(record.getYearOfManufacturing(), "_________________________"));
        data.put("numberOfEarthings", getValueOrDefault(record.getNumberOfEarthings(), "_________________________"));
        data.put("earthResistance", getValueOrDefault(record.getEarthResistance(), "_________________________"));
        data.put("lighteningArrester", getValueOrDefault(record.getLighteningArrester(), "Provided"));
        data.put("vendorName", getValueOrDefault(record.getVendorName(), "_________________________"));
        data.put("vendorSignature", record.getVendorSignature());
        data.put("consumerSignature", record.getConsumerSignature());
        data.put("msedclSignature", record.getMsedclSignature());
        data.put("vendorStamp", record.getVendorStamp());
        data.put("witnessSignature", record.getWitnessSignature());
        data.put("aadharNumber", getValueOrDefault(record.getAadharNumber(), "_________________________"));

        String warrantyDetails = "";
        if (record.getProductWarranty() != null && !record.getProductWarranty().isEmpty()) {
            warrantyDetails = record.getProductWarranty();
        }
        if (record.getPerformanceWarranty() != null && !record.getPerformanceWarranty().isEmpty()) {
            warrantyDetails = warrantyDetails.isEmpty() ? record.getPerformanceWarranty() : warrantyDetails + " / " + record.getPerformanceWarranty();
        }
        data.put("warrantyDetails", warrantyDetails.isEmpty() ? "Standard Warranty" : warrantyDetails);

        String inverterMakeModel = "";
        if (record.getInverterMake() != null && !record.getInverterMake().isEmpty()) {
            inverterMakeModel = record.getInverterMake();
        }
        if (record.getInverterModelNumber() != null && !record.getInverterModelNumber().isEmpty()) {
            inverterMakeModel = inverterMakeModel.isEmpty() ? record.getInverterModelNumber() : inverterMakeModel + " " + record.getInverterModelNumber();
        }
        data.put("inverterMakeModel", inverterMakeModel.isEmpty() ? record.getInverterMake() : inverterMakeModel);

        data.put("meterMake", getValueOrDefault(record.getMeterMake(), "L&T"));
        data.put("acCapacityCheck", getValueOrDefault(record.getAcCapacityCheck(), "Yes"));
        data.put("arrearsStatus", getValueOrDefault(record.getArrearsStatus(), "No"));
        data.put("sldStatus", getValueOrDefault(record.getSldStatus(), "Submitted"));
        data.put("layoutStatus", getValueOrDefault(record.getLayoutStatus(), "Submitted"));
        data.put("earthingDiagram", getValueOrDefault(record.getEarthingDiagram(), "Submitted"));
        data.put("equipmentList", getValueOrDefault(record.getEquipmentList(), "Submitted"));
        data.put("islandingCertificate", getValueOrDefault(record.getIslandingCertificate(), "Submitted"));
        data.put("earthingLA", getValueOrDefault(record.getEarthingLA(), "Provided"));
        data.put("earthingPanel", getValueOrDefault(record.getEarthingPanel(), "Provided"));
        data.put("earthingDCBB", getValueOrDefault(record.getEarthingDCBB(), "Provided"));
        data.put("earthingACBB", getValueOrDefault(record.getEarthingACBB(), "Provided"));
        data.put("earthingInverter", getValueOrDefault(record.getEarthingInverter(), "Provided"));
        data.put("earthingMetering", getValueOrDefault(record.getEarthingMetering(), "Provided"));
        data.put("metallicEarthed", getValueOrDefault(record.getMetallicEarthed(), "Yes"));
        data.put("dcFuses", getValueOrDefault(record.getDcFuses(), "Provided"));
        data.put("acSurge", getValueOrDefault(record.getAcSurge(), "Provided"));
        data.put("acdbSurge", getValueOrDefault(record.getAcdbSurge(), "Provided"));
        data.put("isolationSwitchStatus", getValueOrDefault(record.getIsolationSwitchStatus(), "Yes, with visible verification"));
        data.put("mcbLoad", getValueOrDefault(record.getMcbLoad(), "Provided"));
        data.put("mccbRating", getValueOrDefault(record.getMccbRating(), "32A"));
        data.put("meteringRCCB", getValueOrDefault(record.getMeteringRCCB(), "30mA"));
        data.put("islandingCheck", getValueOrDefault(record.getIslandingCheck(), "Checked"));
        data.put("islandingSatisfactory", getValueOrDefault(record.getIslandingSatisfactory(), "Yes"));
        data.put("backupCheck", getValueOrDefault(record.getBackupCheck(), "Not Applicable"));
        data.put("netInstalled", getValueOrDefault(record.getNetInstalled(), "Yes"));
        data.put("netTesting", getValueOrDefault(record.getNetTesting(), "Yes"));
        data.put("genInstalled", getValueOrDefault(record.getGenInstalled(), "Yes"));
        data.put("genTesting", getValueOrDefault(record.getGenTesting(), "Yes"));
        data.put("genMeterConn", getValueOrDefault(record.getGenMeterConn(), "Correct"));
        data.put("netMeterConn", getValueOrDefault(record.getNetMeterConn(), "Correct"));
        data.put("inverterHealthy", getValueOrDefault(record.getInverterHealthy(), "Yes"));
        data.put("systemTakeover", getValueOrDefault(record.getSystemTakeover(), "Yes"));
        data.put("currentDate", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        List<String> aadharImageUrls = new ArrayList<>();
        if (record.getAadharImages() != null && !record.getAadharImages().isEmpty()) {
            for (String imageUrl : record.getAadharImages()) {
                if (imageUrl != null && imageUrl.startsWith("http")) {
                    aadharImageUrls.add(imageUrl);
                }
            }
        }
        data.put("aadharImageUrls", aadharImageUrls);

        if (includeCompleteData) {
            data.put("meterNumber", getValueOrDefault(record.getMeterNumber(), "_________________________"));
            data.put("netMeterNumber", getValueOrDefault(record.getNetMeterNumber(), "_________________________"));
            data.put("mobileNumber", getValueOrDefault(record.getMobileNumber(), "_________________________"));
            data.put("email", getValueOrDefault(record.getEmail(), "_________________________"));
            data.put("reArrangementType", getValueOrDefault(record.getReArrangementType(), "Net Metering Arrangement"));
            data.put("reSource", getValueOrDefault(record.getReSource(), "Solar"));
            data.put("capacityType", getValueOrDefault(record.getCapacityType(), "Rooftop"));
            data.put("projectModel", getValueOrDefault(record.getProjectModel(), "CAPEX"));
            data.put("reInstalledCapacityRooftop", getValueOrDefault(record.getReInstalledCapacityRooftop(), "_________________________"));
            data.put("reInstalledCapacityRooftopGround", getValueOrDefault(record.getReInstalledCapacityRooftopGround(), "_________________________"));
            data.put("reInstalledCapacityGround", getValueOrDefault(record.getReInstalledCapacityGround(), "_________________________"));
            data.put("moduleSerialNumbers", getValueOrDefault(record.getModuleSerialNumbers(), "_________________________________________________________________________________"));
            data.put("cellManufacturerName", getValueOrDefault(record.getCellManufacturerName(), "_________________________"));
            data.put("cellGSTInvoiceNo", getValueOrDefault(record.getCellGSTInvoiceNo(), "_________________________"));
            data.put("productWarranty", getValueOrDefault(record.getProductWarranty(), "_________________________"));
            data.put("performanceWarranty", getValueOrDefault(record.getPerformanceWarranty(), "_________________________"));
            data.put("inverterMake", getValueOrDefault(record.getInverterMake(), "_________________________"));
            data.put("inverterModelNumber", getValueOrDefault(record.getInverterModelNumber(), "_________________________"));
            data.put("mpptCapacity", getValueOrDefault(record.getMpptCapacity(), "_________________________"));
            data.put("vendorAddress", getValueOrDefault(record.getVendorAddress(), "_________________________________________________________________________________"));
            data.put("authorizedPersonName", getValueOrDefault(record.getAuthorizedPersonName(), "_________________________"));
            data.put("designation", getValueOrDefault(record.getDesignation(), "Authorized Signatory"));

            List<String> aadharBase64Images = new ArrayList<>();
            if (record.getAadharImages() != null && !record.getAadharImages().isEmpty()) {
                for (String imageUrl : record.getAadharImages()) {
                    try {
                        if (imageUrl != null && imageUrl.startsWith("http")) {
                            java.net.URL url = new java.net.URL(imageUrl);
                            byte[] imageBytes = url.openStream().readAllBytes();
                            aadharBase64Images.add(PdfGeneratorService.imageToBase64(imageBytes, "image/jpeg"));
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to load image: " + imageUrl + " - " + e.getMessage());
                    }
                }
            }
            data.put("aadharImagesBase64", aadharBase64Images);

            data.put("location", getValueOrDefault(record.getLocation(), "_________________________"));
            data.put("day", getValueOrDefault(record.getDay(), String.valueOf(now.getDayOfMonth())));
            data.put("month", getValueOrDefault(record.getMonth(), now.getMonth().toString()));
            data.put("year", getValueOrDefault(record.getYear(), String.valueOf(now.getYear())));
            data.put("msedclAddress", getValueOrDefault(record.getMsedclAddress(), "_________________________"));
            data.put("msedclOfficerName", getValueOrDefault(record.getMsedclOfficerName(), "_________________________"));
            data.put("msedclOfficerDesignation", getValueOrDefault(record.getMsedclOfficerDesignation(), "_________________________"));
            data.put("interconnectionPoint", getValueOrDefault(record.getInterconnectionPoint(), "_________________________"));

            String inspectorName = getValueOrDefault(record.getInspectorName(), "");
            if (inspectorName.isEmpty() || inspectorName.equals("_________________________")) {
                inspectorName = record.getVendorName() + " (Inspector)";
            }
            data.put("inspectorName", inspectorName);

            data.put("applicationNumber", getValueOrDefault(record.getApplicationNumber(), record.getSanctionNumber()));
            data.put("applicationDate", getValueOrDefault(record.getApplicationDate(), installationDate));
            data.put("discomName", getValueOrDefault(record.getDiscomName(), "MSEDCL"));
            data.put("place", getValueOrDefault(record.getPlace(), "_________________________"));
            data.put("witness1Name", getValueOrDefault(record.getWitness1Name(), "_________________________"));
            data.put("witness1Address", getValueOrDefault(record.getWitness1Address(), "_________________________________________________________________________________"));
            data.put("witness2Name", getValueOrDefault(record.getWitness2Name(), "_________________________"));
            data.put("witness2Address", getValueOrDefault(record.getWitness2Address(), "_________________________________________________________________________________"));
            data.put("indemnityDay", getValueOrDefault(record.getIndemnityDay(), String.valueOf(now.getDayOfMonth())));
            data.put("indemnityMonth", getValueOrDefault(record.getIndemnityMonth(), now.getMonth().toString()));
            data.put("indemnityYear", getValueOrDefault(record.getIndemnityYear(), String.valueOf(now.getYear())));
            data.put("grReferenceNumber", getValueOrDefault(record.getGrReferenceNumber(), "202510061736312910"));
            data.put("grReferenceDate", getValueOrDefault(record.getGrReferenceDate(), "06th Oct 2025"));
            data.put("pbgAmount", getValueOrDefault(record.getPbgAmount(), "_________________________"));

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
            data.put("aadharImages", record.getAadharImages());
        }

        return data;
    }

    private String getPdfFilename(String type) {
        switch(type) {
            case "wcr": return "WCR_Undertaking_Guarantee_Aadhar.pdf";
            case "proforma-a": return "Annexure-I_Proforma-A.pdf";
            case "dcr": return "Declaration_FOR_DCR.pdf";
            case "agreement": return "NET_METERING_CONNECTION_AGREEMENT.pdf";
            case "indemnity": return "INDEMNITY_BOND.pdf";
            default: return "document_" + System.currentTimeMillis() + ".pdf";
        }
    }

    private String getValueOrDefault(Object value, String defaultValue) {
        if (value == null) return defaultValue;
        String stringValue = String.valueOf(value);
        return stringValue.isEmpty() ? defaultValue : stringValue;
    }
}