package com.suraj.Customer_Portal_29.controller;

import com.suraj.Customer_Portal_29.dto.response.SolarRecordResponseDto;
import com.suraj.Customer_Portal_29.service.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.nio.file.*;
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
        Map<String, Object> data = buildPdfData(id);
        byte[] wordDoc = wordService.generateWord(type, data);

        String filename = type + ".doc";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + filename)
                .contentType(MediaType.TEXT_HTML)
                .body(wordDoc);
    }
    private Map<String, Object> buildPdfData(String id) {
        SolarRecordResponseDto record = solarService.findById(id);
        Map<String, Object> data = new HashMap<>();

        // ================= BASIC INFORMATION =================
        data.put("name", record.getName() != null ? record.getName() : "_________________________");
        data.put("consumerNumber", record.getConsumerNumber() != null ? record.getConsumerNumber() : "_________________________");
        data.put("consumerName", record.getName() != null ? record.getName() : "_________________________");
        data.put("siteAddress", record.getSiteAddress() != null ? record.getSiteAddress() : "_________________________________________________________________________________");
        data.put("category", record.getCategory() != null ? record.getCategory() : "_________________________");
        data.put("sanctionNumber", record.getSanctionNumber() != null ? record.getSanctionNumber() : "_________________________");
        data.put("sanctionedCapacity", record.getSanctionedCapacity() != null ? record.getSanctionedCapacity() : "_________________________");
        data.put("installedCapacity", record.getInstalledCapacity() != null ? record.getInstalledCapacity() : "_________________________");
        data.put("moduleMake", record.getModuleMake() != null ? record.getModuleMake() : "_________________________");
        data.put("almmModelNumber", record.getAlmmModelNumber() != null ? record.getAlmmModelNumber() : "_________________________");
        data.put("wattagePerModule", record.getWattagePerModule() != null ? record.getWattagePerModule() : "_________________________");
        data.put("numberOfModules", record.getNumberOfModules() != null ? record.getNumberOfModules() : "_________________________");
        data.put("totalCapacityKWP", record.getTotalCapacityKWP() != null ? record.getTotalCapacityKWP() : "_________________________");
        data.put("warrantyDetails", (record.getProductWarranty() != null ? record.getProductWarranty() : "") + " / " + (record.getPerformanceWarranty() != null ? record.getPerformanceWarranty() : ""));
        data.put("inverterMakeModel", (record.getInverterMake() != null ? record.getInverterMake() : "") + " " + (record.getInverterModelNumber() != null ? record.getInverterModelNumber() : ""));
        data.put("inverterRating", record.getInverterRating() != null ? record.getInverterRating() : "_________________________");
        data.put("chargeControllerType", record.getChargeControllerType() != null ? record.getChargeControllerType() : "_________________________");
        data.put("inverterCapacity", record.getInverterCapacity() != null ? record.getInverterCapacity() : "_________________________");
        data.put("hpd", record.getHpd() != null ? record.getHpd() : "_________________________");
        data.put("yearOfManufacturing", record.getYearOfManufacturing() != null ? record.getYearOfManufacturing() : "_________________________");
        data.put("numberOfEarthings", record.getNumberOfEarthings() != null ? record.getNumberOfEarthings() : "_________________________");
        data.put("earthResistance", record.getEarthResistance() != null ? record.getEarthResistance() : "_________________________");
        data.put("lighteningArrester", record.getLighteningArrester() != null ? record.getLighteningArrester() : "_________________________");
        data.put("vendorName", record.getVendorName() != null ? record.getVendorName() : "_________________________");
        data.put("vendorStamp", record.getVendorStamp() != null ? record.getVendorStamp() : "_________________________");
        data.put("aadharNumber", record.getAadharNumber() != null ? record.getAadharNumber() : "_________________________");

        // Aadhar images
        List<String> aadharBase64Images = new ArrayList<>();

        if (record.getAadharImages() != null && !record.getAadharImages().isEmpty()) {

            for (String imageUrl : record.getAadharImages()) {
                try {

                    if (imageUrl.startsWith("http")) {
                        java.net.URL url = new java.net.URL(imageUrl);
                        byte[] imageBytes = url.openStream().readAllBytes();

                        aadharBase64Images.add(
                                PdfGeneratorService.imageToBase64(imageBytes, "image/jpeg")
                        );

                    } else {

                        Path imagePath = Paths.get(System.getProperty("user.dir"), imageUrl);

                        if (Files.exists(imagePath)) {
                            byte[] imageBytes = Files.readAllBytes(imagePath);

                            aadharBase64Images.add(
                                    PdfGeneratorService.imageToBase64(imageBytes, "image/jpeg")
                            );
                        }
                    }

                } catch (Exception e) {
                    System.err.println("Failed to load image: " + imageUrl + " - " + e.getMessage());
                }
            }
        }

        data.put("aadharImagesBase64", aadharBase64Images);

        return data;
    }

    @GetMapping("/{id}/{type}")
    public ResponseEntity<byte[]> downloadPdf(
            @PathVariable String id,
            @PathVariable String type) {

        SolarRecordResponseDto record = solarService.findById(id);

        Map<String, Object> data = new HashMap<>();

        // ================= BASIC INFORMATION =================
        data.put("name", record.getName() != null ? record.getName() : "_________________________");
        data.put("consumerNumber", record.getConsumerNumber() != null ? record.getConsumerNumber() : "_________________________");
        data.put("meterNumber", record.getMeterNumber() != null ? record.getMeterNumber() : "_________________________");
        data.put("netMeterNumber", record.getNetMeterNumber() != null ? record.getNetMeterNumber() : "_________________________");
        data.put("mobileNumber", record.getMobileNumber() != null ? record.getMobileNumber() : "_________________________");
        data.put("email", record.getEmail() != null ? record.getEmail() : "_________________________");
        data.put("siteAddress", record.getSiteAddress() != null ? record.getSiteAddress() : "_________________________________________________________________________________");
        data.put("category", record.getCategory() != null ? record.getCategory() : "_________________________");

        // ================= SANCTION DETAILS =================
        data.put("sanctionNumber", record.getSanctionNumber() != null ? record.getSanctionNumber() : "_________________________");
        data.put("sanctionedCapacity", record.getSanctionedCapacity() != null ? record.getSanctionedCapacity() : "_________________________");
        data.put("installedCapacity", record.getInstalledCapacity() != null ? record.getInstalledCapacity() : "_________________________");

        // ================= RE ARRANGEMENT =================
        data.put("reArrangementType", record.getReArrangementType() != null ? record.getReArrangementType() : "Net Metering Arrangement");
        data.put("reSource", record.getReSource() != null ? record.getReSource() : "Solar");
        data.put("capacityType", record.getCapacityType() != null ? record.getCapacityType() : "_________________________");
        data.put("projectModel", record.getProjectModel() != null ? record.getProjectModel() : "_________________________");
        data.put("reInstalledCapacityRooftop", record.getReInstalledCapacityRooftop() != null ? record.getReInstalledCapacityRooftop() : "_________________________");
        data.put("reInstalledCapacityRooftopGround", record.getReInstalledCapacityRooftopGround() != null ? record.getReInstalledCapacityRooftopGround() : "_________________________");
        data.put("reInstalledCapacityGround", record.getReInstalledCapacityGround() != null ? record.getReInstalledCapacityGround() : "_________________________");

        String installationDate = record.getInstallationDate() != null ? record.getInstallationDate() :
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        data.put("installationDate", installationDate);

        // ================= MODULE SPECIFICATIONS =================
        data.put("moduleMake", record.getModuleMake() != null ? record.getModuleMake() : "_________________________");
        data.put("almmModelNumber", record.getAlmmModelNumber() != null ? record.getAlmmModelNumber() : "_________________________");
        data.put("wattagePerModule", record.getWattagePerModule() != null ? record.getWattagePerModule() : "_________________________");
        data.put("numberOfModules", record.getNumberOfModules() != null ? record.getNumberOfModules() : "_________________________");
        data.put("totalCapacityKWP", record.getTotalCapacityKWP() != null ? record.getTotalCapacityKWP() : "_________________________");
        data.put("moduleSerialNumbers", record.getModuleSerialNumbers() != null ? record.getModuleSerialNumbers() : "_________________________________________________________________________________");
        data.put("cellManufacturerName", record.getCellManufacturerName() != null ? record.getCellManufacturerName() : "_________________________");
        data.put("cellGSTInvoiceNo", record.getCellGSTInvoiceNo() != null ? record.getCellGSTInvoiceNo() : "_________________________");

        // ================= WARRANTY DETAILS =================
        data.put("productWarranty", record.getProductWarranty() != null ? record.getProductWarranty() : "_________________________");
        data.put("performanceWarranty", record.getPerformanceWarranty() != null ? record.getPerformanceWarranty() : "_________________________");

        // ================= INVERTER DETAILS =================
        data.put("inverterMake", record.getInverterMake() != null ? record.getInverterMake() : "_________________________");
        data.put("inverterModelNumber", record.getInverterModelNumber() != null ? record.getInverterModelNumber() : "_________________________");
        data.put("inverterRating", record.getInverterRating() != null ? record.getInverterRating() : "_________________________");
        data.put("inverterCapacity", record.getInverterCapacity() != null ? record.getInverterCapacity() : "_________________________");
        data.put("chargeControllerType", record.getChargeControllerType() != null ? record.getChargeControllerType() : "_________________________");
        data.put("mpptCapacity", record.getMpptCapacity() != null ? record.getMpptCapacity() : "_________________________");
        data.put("hpd", record.getHpd() != null ? record.getHpd() : "_________________________");
        data.put("yearOfManufacturing", record.getYearOfManufacturing() != null ? record.getYearOfManufacturing() : "_________________________");

        // ================= EARTHING AND PROTECTIONS =================
        data.put("numberOfEarthings", record.getNumberOfEarthings() != null ? record.getNumberOfEarthings() : "_________________________");
        data.put("earthResistance", record.getEarthResistance() != null ? record.getEarthResistance() : "_________________________");
        data.put("lighteningArrester", record.getLighteningArrester() != null ? record.getLighteningArrester() : "_________________________");

        // ================= VENDOR DETAILS =================
        data.put("vendorName", record.getVendorName() != null ? record.getVendorName() : "_________________________");
        data.put("vendorStamp", record.getVendorStamp() != null ? record.getVendorStamp() : "_________________________");
        data.put("vendorAddress", record.getVendorAddress() != null ? record.getVendorAddress() : "_________________________________________________________________________________");
        data.put("authorizedPersonName", record.getAuthorizedPersonName() != null ? record.getAuthorizedPersonName() : "_________________________");
        data.put("designation", record.getDesignation() != null ? record.getDesignation() : "Authorized Signatory");

        // ================= AADHAR DETAILS =================
        data.put("aadharNumber", record.getAadharNumber() != null ? record.getAadharNumber() : "_________________________");

        List<String> aadharBase64Images = new ArrayList<>();
        if (record.getAadharImages() != null && !record.getAadharImages().isEmpty()) {
            for (String imageUrl : record.getAadharImages()) {
                try {
                    // Check if it's a Cloudinary URL (starts with http)
                    if (imageUrl.startsWith("http")) {
                        // Download image from Cloudinary URL
                        java.net.URL url = new java.net.URL(imageUrl);
                        byte[] imageBytes = url.openStream().readAllBytes();
                        aadharBase64Images.add(PdfGeneratorService.imageToBase64(imageBytes, "image/jpeg"));
                    } else {
                        // Fallback to local file (for old records)
                        Path imagePath = Paths.get(System.getProperty("user.dir"), imageUrl);
                        if (Files.exists(imagePath)) {
                            byte[] imageBytes = Files.readAllBytes(imagePath);
                            aadharBase64Images.add(PdfGeneratorService.imageToBase64(imageBytes, "image/jpeg"));
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Failed to load image: " + imageUrl + " - " + e.getMessage());
                }
            }
        }
        data.put("aadharImagesBase64", aadharBase64Images);


        // ================= AGREEMENT DETAILS =================
        LocalDate now = LocalDate.now();
        data.put("location", record.getLocation() != null ? record.getLocation() : "_________________________");
        data.put("day", record.getDay() != null ? record.getDay() : String.valueOf(now.getDayOfMonth()));
        data.put("month", record.getMonth() != null ? record.getMonth() : now.getMonth().toString());
        data.put("year", record.getYear() != null ? record.getYear() : String.valueOf(now.getYear()));
        data.put("msedclAddress", record.getMsedclAddress() != null ? record.getMsedclAddress() : "_________________________");
        data.put("msedclOfficerName", record.getMsedclOfficerName() != null ? record.getMsedclOfficerName() : "_________________________");
        data.put("msedclOfficerDesignation", record.getMsedclOfficerDesignation() != null ? record.getMsedclOfficerDesignation() : "_________________________");
        data.put("interconnectionPoint", record.getInterconnectionPoint() != null ? record.getInterconnectionPoint() : "_________________________");
        data.put("inspectorName", record.getInspectorName() != null ? record.getInspectorName() : "_________________________");

        // ================= APPLICATION DETAILS =================
        data.put("applicationNumber", record.getApplicationNumber() != null ? record.getApplicationNumber() : record.getSanctionNumber());
        data.put("applicationDate", record.getApplicationDate() != null ? record.getApplicationDate() : installationDate);
        data.put("discomName", record.getDiscomName() != null ? record.getDiscomName() : "MSEDCL");
        data.put("place", record.getPlace() != null ? record.getPlace() : "_________________________");

        // ================= WITNESS DETAILS =================
        data.put("witness1Name", record.getWitness1Name() != null ? record.getWitness1Name() : "_________________________");
        data.put("witness1Address", record.getWitness1Address() != null ? record.getWitness1Address() : "_________________________________________________________________________________");
        data.put("witness2Name", record.getWitness2Name() != null ? record.getWitness2Name() : "_________________________");
        data.put("witness2Address", record.getWitness2Address() != null ? record.getWitness2Address() : "_________________________________________________________________________________");
        data.put("indemnityDay", record.getIndemnityDay() != null ? record.getIndemnityDay() : String.valueOf(LocalDate.now().getDayOfMonth()));
        data.put("indemnityMonth", record.getIndemnityMonth() != null ? record.getIndemnityMonth() : LocalDate.now().getMonth().toString());
        data.put("indemnityYear", record.getIndemnityYear() != null ? record.getIndemnityYear() : String.valueOf(LocalDate.now().getYear()));
        data.put("grReferenceNumber", record.getGrReferenceNumber() != null ? record.getGrReferenceNumber() : "202510061736312910");
        data.put("grReferenceDate", record.getGrReferenceDate() != null ? record.getGrReferenceDate() : "06th Oct 2025");
        data.put("pbgAmount", record.getPbgAmount() != null ? record.getPbgAmount() : "_________________________");
        // ================= PHOTOS =================
        data.put("sitePhotos", record.getSitePhotos());

        byte[] pdf = pdfService.generatePdfAsync(type, data).join();

        String filename = type;
        switch(type) {
            case "wcr": filename = "WCR_Undertaking_Guarantee_Aadhar.pdf"; break;
            case "proforma-a": filename = "Annexure-I_Proforma-A.pdf"; break;
            case "dcr": filename = "Declaration_FOR_DCR.pdf"; break;
            case "agreement": filename = "NET_METERING_CONNECTION_AGREEMENT.pdf"; break;
            case "indemnity": filename = "INDEMNITY_BOND.pdf"; break;
//            case "indemnity": case "INDEMNITY_BOND": filename = "INDEMNITY_BOND.pdf"; type = "indemnity"; break;
            default: filename = "document_" + System.currentTimeMillis() + ".pdf";
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
    @GetMapping("/{id}/all-in-one")
    public ResponseEntity<byte[]> downloadAllInOnePdf(@PathVariable String id) {
        try {
            Map<String, Object> data = buildCompleteData(id);

            List<byte[]> pdfs = new ArrayList<>();
            pdfs.add(pdfService.generatePdf("wcr", data));
            pdfs.add(pdfService.generatePdf("proforma-a", data));
            pdfs.add(pdfService.generatePdf("dcr", data));
            pdfs.add(pdfService.generatePdf("agreement", data));
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

    private Map<String, Object> buildCompleteData(String id) {

        SolarRecordResponseDto record = solarService.findById(id);
        Map<String, Object> data = new HashMap<>();

        // BASIC
        data.put("name", record.getName());
        data.put("consumerNumber", record.getConsumerNumber());
        data.put("meterNumber", record.getMeterNumber());
        data.put("netMeterNumber", record.getNetMeterNumber());
        data.put("mobileNumber", record.getMobileNumber());
        data.put("email", record.getEmail());
        data.put("siteAddress", record.getSiteAddress());
        data.put("category", record.getCategory());

        // SANCTION
        data.put("sanctionNumber", record.getSanctionNumber());
        data.put("sanctionedCapacity", record.getSanctionedCapacity());
        data.put("installedCapacity", record.getInstalledCapacity());

        // SYSTEM
        data.put("reArrangementType", record.getReArrangementType());
        data.put("reSource", record.getReSource());
        data.put("capacityType", record.getCapacityType());
        data.put("projectModel", record.getProjectModel());

        data.put("reInstalledCapacityRooftop", record.getReInstalledCapacityRooftop());
        data.put("reInstalledCapacityRooftopGround", record.getReInstalledCapacityRooftopGround());
        data.put("reInstalledCapacityGround", record.getReInstalledCapacityGround());

        data.put("installationDate", record.getInstallationDate());

        // MODULE
        data.put("moduleMake", record.getModuleMake());
        data.put("almmModelNumber", record.getAlmmModelNumber());
        data.put("wattagePerModule", record.getWattagePerModule());
        data.put("numberOfModules", record.getNumberOfModules());
        data.put("totalCapacityKWP", record.getTotalCapacityKWP());
        data.put("moduleSerialNumbers", record.getModuleSerialNumbers());
        data.put("cellManufacturerName", record.getCellManufacturerName());
        data.put("cellGSTInvoiceNo", record.getCellGSTInvoiceNo());

        // WARRANTY
        data.put("productWarranty", record.getProductWarranty());
        data.put("performanceWarranty", record.getPerformanceWarranty());

        // INVERTER
        data.put("inverterMake", record.getInverterMake());
        data.put("inverterModelNumber", record.getInverterModelNumber());
        data.put("inverterRating", record.getInverterRating());
        data.put("inverterCapacity", record.getInverterCapacity());
        data.put("chargeControllerType", record.getChargeControllerType());
        data.put("mpptCapacity", record.getMpptCapacity());
        data.put("hpd", record.getHpd());
        data.put("yearOfManufacturing", record.getYearOfManufacturing());

        // EARTHING
        data.put("numberOfEarthings", record.getNumberOfEarthings());
        data.put("earthResistance", record.getEarthResistance());
        data.put("lighteningArrester", record.getLighteningArrester());

        // VENDOR
        data.put("vendorName", record.getVendorName());
        data.put("vendorStamp", record.getVendorStamp());
        data.put("vendorAddress", record.getVendorAddress());
        data.put("authorizedPersonName", record.getAuthorizedPersonName());
        data.put("designation", record.getDesignation());

        // LEGAL
        data.put("location", record.getLocation());
        data.put("day", record.getDay());
        data.put("month", record.getMonth());
        data.put("year", record.getYear());

        data.put("msedclAddress", record.getMsedclAddress());
        data.put("msedclOfficerName", record.getMsedclOfficerName());
        data.put("msedclOfficerDesignation", record.getMsedclOfficerDesignation());

        data.put("interconnectionPoint", record.getInterconnectionPoint());
        data.put("inspectorName", record.getInspectorName());

        data.put("applicationNumber", record.getApplicationNumber());
        data.put("applicationDate", record.getApplicationDate());
        data.put("discomName", record.getDiscomName());
        data.put("place", record.getPlace());

        // WITNESS
        data.put("witness1Name", record.getWitness1Name());
        data.put("witness1Address", record.getWitness1Address());
        data.put("witness2Name", record.getWitness2Name());
        data.put("witness2Address", record.getWitness2Address());

        data.put("indemnityDay", record.getIndemnityDay());
        data.put("indemnityMonth", record.getIndemnityMonth());
        data.put("indemnityYear", record.getIndemnityYear());

        data.put("grReferenceNumber", record.getGrReferenceNumber());
        data.put("grReferenceDate", record.getGrReferenceDate());
        data.put("pbgAmount", record.getPbgAmount());

        data.put("aadharNumber", record.getAadharNumber());

        data.put("sitePhotos", record.getSitePhotos());
        List<String> aadharBase64Images = new ArrayList<>();

        if (record.getAadharImages() != null) {
            for (String imageUrl : record.getAadharImages()) {
                try {
                    if (imageUrl.startsWith("http")) {
                        java.net.URL url = new java.net.URL(imageUrl);
                        byte[] imageBytes = url.openStream().readAllBytes();

                        aadharBase64Images.add(
                                PdfGeneratorService.imageToBase64(imageBytes, "image/jpeg")
                        );
                    }
                } catch (Exception e) {
                    System.out.println("Image load failed");
                }
            }
        }

        data.put("aadharImagesBase64", aadharBase64Images);

        return data;
    }
}