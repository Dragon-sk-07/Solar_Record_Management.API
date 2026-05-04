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
        Map<String, Object> data = buildCommonData(id);
        byte[] wordDoc = wordService.generateWord(type, data);
        String filename = type + ".doc";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.TEXT_HTML)
                .body(wordDoc);
    }

    @GetMapping("/{id}/{type}")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable String id, @PathVariable String type) {
        Map<String, Object> data = buildCompleteData(id);
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
            Map<String, Object> data = buildCompleteData(id);
            List<byte[]> pdfs = new ArrayList<>();
            pdfs.add(pdfService.generatePdf("FrontSix", data));
            pdfs.add(pdfService.generatePdf("wcr", data));
            pdfs.add(pdfService.generatePdf("proforma-a", data));
            pdfs.add(pdfService.generatePdf("dcr", data));
            pdfs.add(pdfService.generatePdf("agreement", data));
//            pdfs.add(pdfService.generatePdf("indemnity", data));
            pdfs.add(pdfService.generatePdf("site-photos", data));
            byte[] mergedPdf = pdfMergerService.mergePdfs(pdfs);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=All_In_One.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(mergedPdf);
        } catch (Exception e) {
            Map<String, Object> data = buildCompleteData(id);
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
            Map<String, Object> data = buildCompleteData(id);
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

    private Map<String, Object> buildCommonData(String id) {
        SolarRecordResponseDto record = solarService.findById(id);
        Map<String, Object> data = new HashMap<>();

        data.put("name", getValueOrDefault(record.getName(), "_________________________"));
        data.put("consumerNumber", getValueOrDefault(record.getConsumerNumber(), "_________________________"));
        data.put("consumerName", getValueOrDefault(record.getName(), "_________________________"));
        data.put("siteAddress", getValueOrDefault(record.getSiteAddress(), "_________________________________________________________________________________"));
        data.put("category", getValueOrDefault(record.getCategory(), "_________________________"));
        data.put("sanctionNumber", getValueOrDefault(record.getSanctionNumber(), "_________________________"));
        data.put("sanctionedCapacity", getValueOrDefault(record.getSanctionedCapacity(), "_________________________"));
        data.put("installedCapacity", getValueOrDefault(record.getInstalledCapacity(), "_________________________"));
        data.put("moduleMake", getValueOrDefault(record.getModuleMake(), "_________________________"));
        data.put("almmModelNumber", getValueOrDefault(record.getAlmmModelNumber(), "_________________________"));
        data.put("wattagePerModule", getValueOrDefault(record.getWattagePerModule(), "_________________________"));
        data.put("numberOfModules", getValueOrDefault(record.getNumberOfModules(), "_________________________"));
        data.put("totalCapacityKWP", getValueOrDefault(record.getTotalCapacityKWP(), "_________________________"));
        data.put("warrantyDetails", (record.getProductWarranty() != null ? record.getProductWarranty() : "") + " / " + (record.getPerformanceWarranty() != null ? record.getPerformanceWarranty() : ""));
        data.put("inverterMakeModel", (record.getInverterMake() != null ? record.getInverterMake() : "") + " " + (record.getInverterModelNumber() != null ? record.getInverterModelNumber() : ""));
        data.put("inverterRating", getValueOrDefault(record.getInverterRating(), "_________________________"));
        data.put("chargeControllerType", getValueOrDefault(record.getChargeControllerType(), "_________________________"));
        data.put("inverterCapacity", getValueOrDefault(record.getInverterCapacity(), "_________________________"));
        data.put("hpd", getValueOrDefault(record.getHpd(), "_________________________"));
        data.put("yearOfManufacturing", getValueOrDefault(record.getYearOfManufacturing(), "_________________________"));
        data.put("numberOfEarthings", getValueOrDefault(record.getNumberOfEarthings(), "_________________________"));
        data.put("earthResistance", getValueOrDefault(record.getEarthResistance(), "_________________________"));
        data.put("lighteningArrester", getValueOrDefault(record.getLighteningArrester(), "_________________________"));
        data.put("vendorName", getValueOrDefault(record.getVendorName(), "_________________________"));
        data.put("vendorStamp", record.getVendorStamp());
        data.put("vendorSignature", record.getVendorSignature());
        data.put("consumerSignature", record.getConsumerSignature());
        data.put("msedclSignature", record.getMsedclSignature());
        data.put("witnessSignature", record.getWitnessSignature());
        data.put("aadharNumber", getValueOrDefault(record.getAadharNumber(), "_________________________"));

        List<String> aadharImageUrls = new ArrayList<>();
        if (record.getAadharImages() != null && !record.getAadharImages().isEmpty()) {
            for (String imageUrl : record.getAadharImages()) {
                if (imageUrl != null && imageUrl.startsWith("http")) {
                    aadharImageUrls.add(imageUrl);
                }
            }
        }
        data.put("aadharImageUrls", aadharImageUrls);

        return data;
    }

    private Map<String, Object> buildCompleteData(String id) {
        SolarRecordResponseDto record = solarService.findById(id);
        Map<String, Object> data = new HashMap<>();
        LocalDate now = LocalDate.now();

        data.put("name", getValueOrDefault(record.getName(), "_________________________"));
        data.put("consumerNumber", getValueOrDefault(record.getConsumerNumber(), "_________________________"));
        data.put("meterNumber", getValueOrDefault(record.getMeterNumber(), "_________________________"));
        data.put("netMeterNumber", getValueOrDefault(record.getNetMeterNumber(), "_________________________"));
        data.put("mobileNumber", getValueOrDefault(record.getMobileNumber(), "_________________________"));
        data.put("email", getValueOrDefault(record.getEmail(), "_________________________"));
        data.put("siteAddress", getValueOrDefault(record.getSiteAddress(), "_________________________________________________________________________________"));
        data.put("category", getValueOrDefault(record.getCategory(), "_________________________"));
        data.put("sanctionNumber", getValueOrDefault(record.getSanctionNumber(), "_________________________"));
        data.put("sanctionedCapacity", getValueOrDefault(record.getSanctionedCapacity(), "_________________________"));
        data.put("installedCapacity", getValueOrDefault(record.getInstalledCapacity(), "_________________________"));
        data.put("reArrangementType", getValueOrDefault(record.getReArrangementType(), "Net Metering Arrangement"));
        data.put("reSource", getValueOrDefault(record.getReSource(), "Solar"));
        data.put("capacityType", getValueOrDefault(record.getCapacityType(), "_________________________"));
        data.put("projectModel", getValueOrDefault(record.getProjectModel(), "_________________________"));
        data.put("reInstalledCapacityRooftop", getValueOrDefault(record.getReInstalledCapacityRooftop(), "_________________________"));
        data.put("reInstalledCapacityRooftopGround", getValueOrDefault(record.getReInstalledCapacityRooftopGround(), "_________________________"));
        data.put("reInstalledCapacityGround", getValueOrDefault(record.getReInstalledCapacityGround(), "_________________________"));

        String installationDate = getValueOrDefault(record.getInstallationDate(), LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        data.put("installationDate", installationDate);

        data.put("moduleMake", getValueOrDefault(record.getModuleMake(), "_________________________"));
        data.put("almmModelNumber", getValueOrDefault(record.getAlmmModelNumber(), "_________________________"));
        data.put("wattagePerModule", getValueOrDefault(record.getWattagePerModule(), "_________________________"));
        data.put("numberOfModules", getValueOrDefault(record.getNumberOfModules(), "_________________________"));
        data.put("totalCapacityKWP", getValueOrDefault(record.getTotalCapacityKWP(), "_________________________"));
        data.put("moduleSerialNumbers", getValueOrDefault(record.getModuleSerialNumbers(), "_________________________________________________________________________________"));
        data.put("cellManufacturerName", getValueOrDefault(record.getCellManufacturerName(), "_________________________"));
        data.put("cellGSTInvoiceNo", getValueOrDefault(record.getCellGSTInvoiceNo(), "_________________________"));
        data.put("productWarranty", getValueOrDefault(record.getProductWarranty(), "_________________________"));
        data.put("performanceWarranty", getValueOrDefault(record.getPerformanceWarranty(), "_________________________"));
        data.put("inverterMake", getValueOrDefault(record.getInverterMake(), "_________________________"));
        data.put("inverterModelNumber", getValueOrDefault(record.getInverterModelNumber(), "_________________________"));
        data.put("inverterRating", getValueOrDefault(record.getInverterRating(), "_________________________"));
        data.put("inverterCapacity", getValueOrDefault(record.getInverterCapacity(), "_________________________"));
        data.put("chargeControllerType", getValueOrDefault(record.getChargeControllerType(), "_________________________"));
        data.put("mpptCapacity", getValueOrDefault(record.getMpptCapacity(), "_________________________"));
        data.put("hpd", getValueOrDefault(record.getHpd(), "_________________________"));
        data.put("yearOfManufacturing", getValueOrDefault(record.getYearOfManufacturing(), "_________________________"));
        data.put("numberOfEarthings", getValueOrDefault(record.getNumberOfEarthings(), "_________________________"));
        data.put("earthResistance", getValueOrDefault(record.getEarthResistance(), "_________________________"));
        data.put("lighteningArrester", getValueOrDefault(record.getLighteningArrester(), "_________________________"));
        data.put("vendorName", getValueOrDefault(record.getVendorName(), "_________________________"));
        data.put("vendorAddress", getValueOrDefault(record.getVendorAddress(), "_________________________________________________________________________________"));
        data.put("authorizedPersonName", getValueOrDefault(record.getAuthorizedPersonName(), "_________________________"));
        data.put("designation", getValueOrDefault(record.getDesignation(), "Authorized Signatory"));
        data.put("vendorSignature", record.getVendorSignature());
        data.put("consumerSignature", record.getConsumerSignature());
        data.put("msedclSignature", record.getMsedclSignature());
        data.put("vendorStamp", record.getVendorStamp());
        data.put("witnessSignature", record.getWitnessSignature());
        data.put("aadharNumber", getValueOrDefault(record.getAadharNumber(), "_________________________"));

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
        data.put("inspectorName", getValueOrDefault(record.getInspectorName(), "_________________________"));
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
        data.put("sitePhotos", record.getSitePhotos());
        data.put("aadharImages", record.getAadharImages());

        List<String> aadharImageUrls = new ArrayList<>();
        if (record.getAadharImages() != null && !record.getAadharImages().isEmpty()) {
            for (String imageUrl : record.getAadharImages()) {
                if (imageUrl != null && imageUrl.startsWith("http")) {
                    aadharImageUrls.add(imageUrl);
                }
            }
        }
        data.put("aadharImageUrls", aadharImageUrls);

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
        return value != null ? String.valueOf(value) : defaultValue;
    }
}