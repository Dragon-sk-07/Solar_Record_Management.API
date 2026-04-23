package com.suraj.Customer_Portal_29.controller;

import com.suraj.Customer_Portal_29.dto.response.SolarRecordResponseDto;
import com.suraj.Customer_Portal_29.service.PdfGeneratorService;
import com.suraj.Customer_Portal_29.service.SolarRecordService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/solar/pdf")
public class SolarPdfController {

    private final SolarRecordService solarService;
    private final PdfGeneratorService pdfService;

    public SolarPdfController(SolarRecordService solarService,
                              PdfGeneratorService pdfService) {
        this.solarService = solarService;
        this.pdfService = pdfService;
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
            for (String imagePathStr : record.getAadharImages()) {
                try {
                    Path imagePath = Paths.get(System.getProperty("user.dir"), imagePathStr);
                    if (Files.exists(imagePath)) {
                        byte[] imageBytes = Files.readAllBytes(imagePath);
                        aadharBase64Images.add(PdfGeneratorService.imageToBase64(imageBytes, "image/jpeg"));
                    }
                } catch (Exception e) {
                    // Skip failed images
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
            default: filename = "document_" + System.currentTimeMillis() + ".pdf";
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}