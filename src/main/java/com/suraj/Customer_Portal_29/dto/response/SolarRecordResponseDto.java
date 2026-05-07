package com.suraj.Customer_Portal_29.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SolarRecordResponseDto {

    private String id;
    private String createdAt;

    private String name;
    private String consumerNumber;
    private String mobileNumber;
    private String email;
    private String siteAddress;
    private String category;
    private String aadharNumber;

    private String sanctionNumber;
    private Double sanctionedCapacity;
    private Double installedCapacity;

    private String reArrangementType;
    private String reSource;
    private String capacityType;
    private String projectModel;
    private Double reInstalledCapacityRooftop;
    private Double reInstalledCapacityRooftopGround;
    private Double reInstalledCapacityGround;
    private String installationDate;

    private String moduleMake;
    private String almmModelNumber;
    private Double wattagePerModule;
    private Integer numberOfModules;
    private Double totalCapacityKWP;
    private String moduleSerialNumbers;
    private String cellManufacturerName;
    private String cellGSTInvoiceNo;

    private String productWarranty;
    private String performanceWarranty;

    private String inverterMake;
    private String inverterModelNumber;
    private Double inverterRating;
    private Double inverterCapacity;
    private String chargeControllerType;
    private Double mpptCapacity;
    private String hpd;
    private Integer yearOfManufacturing;

    private Integer numberOfEarthings;
    private Double earthResistance;
    private String lighteningArrester;

    private String vendorName;
    private String vendorAddress;
    private String authorizedPersonName;
    private String designation;

    private String msedclAddress;
    private String msedclOfficerName;
    private String msedclOfficerDesignation;
    private String inspectorName;

    private String location;
    private String day;
    private String month;
    private String year;
    private String interconnectionPoint;

    private String applicationNumber;
    private String applicationDate;
    private String discomName;
    private String place;

    private String witness1Name;
    private String witness1Address;
    private String witness2Name;
    private String witness2Address;

    private String meterNumber;
    private String netMeterNumber;

    private String indemnityDay;
    private String indemnityMonth;
    private String indemnityYear;
    private String grReferenceNumber;
    private String grReferenceDate;
    private Double pbgAmount;

    // ==================== METER DETAILS ====================
    private String meterMake;
    private String mccbRating;
    private String meteringRCCB;

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
    private List<String> vendorStamp;
    private List<String> vendorSignature;
    private List<String> consumerSignature;
    private List<String> msedclSignature;
    private List<String> witnessSignature;
    private List<String> sitePhotos;
    private List<String> aadharImages;
}