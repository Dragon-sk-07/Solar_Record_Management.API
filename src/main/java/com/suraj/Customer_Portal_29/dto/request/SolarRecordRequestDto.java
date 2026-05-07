package com.suraj.Customer_Portal_29.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Data
public class SolarRecordRequestDto {

    // ==================== BASIC INFORMATION ====================
    @NotBlank(message = "Consumer name is required")
    @Size(max = 100, message = "Consumer name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Consumer number is required")
    private String consumerNumber;

    @NotBlank(message = "Mobile number is required")
    @Size(min = 10, max = 10, message = "Mobile number must be 10 digits")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Mobile number must be a valid Indian number")
    private String mobileNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Site address is required")
    @Size(max = 1000, message = "Site address must not exceed 1000 characters")
    private String siteAddress;

    @NotBlank(message = "Category is required")
    private String category;

    @Pattern(regexp = "^\\d{4}\\s\\d{4}\\s\\d{4}$", message = "Aadhar number must be in format XXXX XXXX XXXX")
    private String aadharNumber;

    // ==================== SANCTION DETAILS ====================
    @NotBlank(message = "Sanction number is required")
    private String sanctionNumber;

    @NotNull(message = "Sanctioned capacity is required")
    @DecimalMin(value = "0.1", message = "Sanctioned capacity must be greater than 0")
    private Double sanctionedCapacity;

    @NotNull(message = "Installed capacity is required")
    @DecimalMin(value = "0.1", message = "Installed capacity must be greater than 0")
    private Double installedCapacity;

    // ==================== RE ARRANGEMENT ====================
    @NotBlank(message = "RE arrangement type is required")
    private String reArrangementType;

    @NotBlank(message = "RE source is required")
    private String reSource;

    @NotBlank(message = "Capacity type is required")
    private String capacityType;

    @NotBlank(message = "Project model is required")
    private String projectModel;

    @NotNull(message = "Rooftop capacity is required")
    private Double reInstalledCapacityRooftop;

    @NotNull(message = "Rooftop + Ground capacity is required")
    private Double reInstalledCapacityRooftopGround;

    @NotNull(message = "Ground capacity is required")
    private Double reInstalledCapacityGround;

    @NotNull(message = "Installation date is required")
    private LocalDate installationDate;

    // ==================== MODULE SPECIFICATIONS ====================
    @NotBlank(message = "Module make is required")
    private String moduleMake;

    @NotBlank(message = "ALMM model number is required")
    private String almmModelNumber;

    @NotNull(message = "Wattage per module is required")
    private Double wattagePerModule;

    @NotNull(message = "Number of modules is required")
    @Min(value = 1, message = "Number of modules must be at least 1")
    private Integer numberOfModules;

    @NotNull(message = "Total capacity is required")
    private Double totalCapacityKWP;

    @NotBlank(message = "Module serial numbers are required")
    @Size(max = 2000, message = "Module serial numbers must not exceed 2000 characters")
    private String moduleSerialNumbers;

    @NotBlank(message = "Cell manufacturer name is required")
    private String cellManufacturerName;

    @NotBlank(message = "Cell GST invoice number is required")
    private String cellGSTInvoiceNo;

    // ==================== WARRANTY DETAILS ====================
    @NotBlank(message = "Product warranty details are required")
    @Size(max = 1000, message = "Product warranty details must not exceed 1000 characters")
    private String productWarranty;

    @NotBlank(message = "Performance warranty details are required")
    @Size(max = 1000, message = "Performance warranty details must not exceed 1000 characters")
    private String performanceWarranty;

    // ==================== INVERTER DETAILS ====================
    @NotBlank(message = "Inverter make is required")
    private String inverterMake;

    @NotBlank(message = "Inverter model number is required")
    private String inverterModelNumber;

    @NotNull(message = "Inverter rating is required")
    private Double inverterRating;

    @NotNull(message = "Inverter capacity is required")
    private Double inverterCapacity;

    @NotBlank(message = "Charge controller type is required")
    private String chargeControllerType;

    @NotNull(message = "MPPT capacity is required")
    private Double mpptCapacity;

    @NotBlank(message = "HPD is required")
    private String hpd;

    @NotNull(message = "Year of manufacturing is required")
    @Min(value = 2000, message = "Year of manufacturing must be valid")
    @Max(value = 2100, message = "Year of manufacturing must be valid")
    private Integer yearOfManufacturing;

    // ==================== EARTHING AND PROTECTIONS ====================
    @NotNull(message = "Number of earthings is required")
    private Integer numberOfEarthings;

    @NotNull(message = "Earth resistance is required")
    private Double earthResistance;

    @NotBlank(message = "Lightning arrester details are required")
    private String lighteningArrester;

    // ==================== VENDOR DETAILS ====================
    @NotBlank(message = "Vendor name is required")
    private String vendorName;

    @Size(max = 1000, message = "Vendor address must not exceed 1000 characters")
    private String vendorAddress;

    private String authorizedPersonName;

    private String designation;

    // ==================== MSEDCL & INSPECTOR DETAILS ====================
    @Size(max = 1000, message = "MSEDCL address must not exceed 1000 characters")
    private String msedclAddress;

    private String msedclOfficerName;

    private String msedclOfficerDesignation;

    private String inspectorName;

    // ==================== AGREEMENT DETAILS ====================
    private String location;

    private String day;

    private String month;

    private String year;

    private String interconnectionPoint;

    // ==================== APPLICATION DETAILS ====================
    private String applicationNumber;

    private LocalDate applicationDate;

    private String discomName;

    private String place;

    // ==================== WITNESS DETAILS ====================
    private String witness1Name;

    @Size(max = 1000, message = "Witness 1 address must not exceed 1000 characters")
    private String witness1Address;

    private String witness2Name;

    @Size(max = 1000, message = "Witness 2 address must not exceed 1000 characters")
    private String witness2Address;

    // ==================== METER DETAILS ====================
    private String meterNumber;
    private String netMeterNumber;
    private String meterMake;
    private String mccbRating;
    private String meteringRCCB;

    // ==================== INDEMNITY DETAILS ====================
    private String indemnityDay;
    private String indemnityMonth;
    private String indemnityYear;

    // ==================== GR REFERENCE DETAILS ====================
    private String grReferenceNumber;
    private String grReferenceDate;

    // ==================== PBG AMOUNT ====================
    private Double pbgAmount;

    // ==================== AC CAPACITY & ARREARS ====================
    private String acCapacityCheck;
    private String arrearsStatus;

    // ==================== DOCUMENT STATUS ====================
    private String sldStatus;
    private String layoutStatus;
    private String earthingDiagram;
    private String equipmentList;
    private String islandingCertificate;

    // ==================== EARTHING DETAILS ====================
    private String earthingLA;
    private String earthingPanel;
    private String earthingDCBB;
    private String earthingACBB;
    private String earthingInverter;
    private String earthingMetering;
    private String metallicEarthed;

    // ==================== FUSES & SURGE DETAILS ====================
    private String dcFuses;
    private String acSurge;
    private String acdbSurge;

    // ==================== ISOLATION & LOAD DETAILS ====================
    private String isolationSwitchStatus;
    private String mcbLoad;

    // ==================== ISLANDING CHECKS ====================
    private String islandingCheck;
    private String islandingSatisfactory;
    private String backupCheck;

    // ==================== METER CONNECTION DETAILS ====================
    private String genMeterConn;
    private String netMeterConn;

    // ==================== INVERTER HEALTH ====================
    private String inverterHealthy;
    private String systemTakeover;

    // ==================== INSTALLATION & TESTING STATUS ====================
    private String netInstalled;
    private String netTesting;
    private String genInstalled;
    private String genTesting;

    // ==================== FILE UPLOADS ====================
    private List<MultipartFile> vendorSignature;
    private List<MultipartFile> consumerSignature;
    private List<MultipartFile> msedclSignature;
    private List<MultipartFile> vendorStamp;
    private List<MultipartFile> witnessSignature;
    private List<MultipartFile> aadharImages;
    private List<MultipartFile> sitePhotos;

    // ==================== EXISTING FILES (FOR UPDATE) ====================
    private List<String> existingVendorSignature;
    private List<String> existingConsumerSignature;
    private List<String> existingMsedclSignature;
    private List<String> existingVendorStamp;
    private List<String> existingWitnessSignature;
    private List<String> existingSitePhotos;
    private List<String> existingPhotos;
    private List<String> existingAadharImages;
}