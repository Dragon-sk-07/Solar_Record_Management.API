package com.suraj.Customer_Portal_29.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "solar_records")
public class SolarRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    private String name;

    @Column(unique = true, nullable = false)
    private String consumerNumber;

    private String mobileNumber;
    private String email;

    @Column(length = 1000)
    private String siteAddress;

    private String category;

    @Column(name = "aadhar_number")
    private String aadharNumber;

    @Column(name = "sanction_number")
    private String sanctionNumber;

    @Column(name = "sanctioned_capacity")
    private Double sanctionedCapacity;

    @NotNull
    @DecimalMin("0.1")
    private Double installedCapacity;

    @Column(name = "re_arrangement_type")
    private String reArrangementType;

    @Column(name = "re_source")
    private String reSource;

    @Column(name = "capacity_type")
    private String capacityType;

    @Column(name = "project_model")
    private String projectModel;

    @Column(name = "re_installed_capacity_rooftop")
    private Double reInstalledCapacityRooftop;

    @Column(name = "re_installed_capacity_rooftop_ground")
    private Double reInstalledCapacityRooftopGround;

    @Column(name = "re_installed_capacity_ground")
    private Double reInstalledCapacityGround;

    @Column(name = "installation_date")
    private LocalDate installationDate;

    @Column(name = "module_make")
    private String moduleMake;

    @Column(name = "almm_model_number")
    private String almmModelNumber;

    @Column(name = "wattage_per_module")
    private Double wattagePerModule;

    @Column(name = "number_of_modules")
    private Integer numberOfModules;

    @Column(name = "total_capacity_kwp")
    private Double totalCapacityKWP;

    @Column(name = "module_serial_numbers", length = 2000)
    private String moduleSerialNumbers;

    @Column(name = "cell_manufacturer_name")
    private String cellManufacturerName;

    @Column(name = "cell_gst_invoice_no")
    private String cellGSTInvoiceNo;

    @Column(name = "product_warranty", length = 1000)
    private String productWarranty;

    @Column(name = "performance_warranty", length = 1000)
    private String performanceWarranty;

    @Column(name = "inverter_make")
    private String inverterMake;

    @Column(name = "inverter_model_number")
    private String inverterModelNumber;

    @Column(name = "inverter_rating")
    private Double inverterRating;

    @Column(name = "inverter_capacity")
    private Double inverterCapacity;

    @Column(name = "charge_controller_type")
    private String chargeControllerType;

    @Column(name = "mppt_capacity")
    private Double mpptCapacity;

    @Column(name = "hpd")
    private String hpd;

    @Column(name = "year_of_manufacturing")
    private Integer yearOfManufacturing;

    @Column(name = "number_of_earthings")
    private Integer numberOfEarthings;

    @Column(name = "earth_resistance")
    private Double earthResistance;

    @Column(name = "lightening_arrester")
    private String lighteningArrester;

    @Column(name = "vendor_name")
    private String vendorName;

    @Column(name = "vendor_address", length = 1000)
    private String vendorAddress;

    @Column(name = "authorized_person_name")
    private String authorizedPersonName;

    @Column(name = "designation")
    private String designation;

    @Column(name = "msedcl_address", length = 1000)
    private String msedclAddress;

    @Column(name = "msedcl_officer_name")
    private String msedclOfficerName;

    @Column(name = "msedcl_officer_designation")
    private String msedclOfficerDesignation;

    @Column(name = "inspector_name")
    private String inspectorName;

    @Column(name = "location")
    private String location;

    @Column(name = "agreement_day")
    private String day;

    @Column(name = "agreement_month")
    private String month;

    @Column(name = "agreement_year")
    private String year;

    @Column(name = "interconnection_point")
    private String interconnectionPoint;

    @Column(name = "application_number")
    private String applicationNumber;

    @Column(name = "application_date")
    private LocalDate applicationDate;

    @Column(name = "discom_name")
    private String discomName;

    @Column(name = "place")
    private String place;

    @Column(name = "witness1_name")
    private String witness1Name;

    @Column(name = "witness1_address", length = 1000)
    private String witness1Address;

    @Column(name = "witness2_name")
    private String witness2Name;

    @Column(name = "witness2_address", length = 1000)
    private String witness2Address;

    @Column(name = "meter_number")
    private String meterNumber;

    @Column(name = "net_meter_number")
    private String netMeterNumber;

    @Column(name = "meter_make")
    private String meterMake;

    @Column(name = "mccb_rating")
    private String mccbRating;

    @Column(name = "metering_rccb")
    private String meteringRCCB;

    @Column(name = "indemnity_day")
    private String indemnityDay;

    @Column(name = "indemnity_month")
    private String indemnityMonth;

    @Column(name = "indemnity_year")
    private String indemnityYear;

    @Column(name = "gr_reference_number")
    private String grReferenceNumber;

    @Column(name = "gr_reference_date")
    private String grReferenceDate;

    @Column(name = "pbg_amount")
    private Double pbgAmount;

    @Column(name = "ac_capacity_check")
    private String acCapacityCheck;

    @Column(name = "arrears_status")
    private String arrearsStatus;

    @Column(name = "sld_status")
    private String sldStatus;

    @Column(name = "layout_status")
    private String layoutStatus;

    @Column(name = "earthing_diagram")
    private String earthingDiagram;

    @Column(name = "equipment_list")
    private String equipmentList;

    @Column(name = "islanding_certificate")
    private String islandingCertificate;

    @Column(name = "earthing_la")
    private String earthingLA;

    @Column(name = "earthing_panel")
    private String earthingPanel;

    @Column(name = "earthing_dcbb")
    private String earthingDCBB;

    @Column(name = "earthing_acbb")
    private String earthingACBB;

    @Column(name = "earthing_inverter")
    private String earthingInverter;

    @Column(name = "earthing_metering")
    private String earthingMetering;

    @Column(name = "metallic_earthed")
    private String metallicEarthed;

    @Column(name = "dc_fuses")
    private String dcFuses;

    @Column(name = "ac_surge")
    private String acSurge;

    @Column(name = "acdb_surge")
    private String acdbSurge;

    @Column(name = "isolation_switch_status")
    private String isolationSwitchStatus;

    @Column(name = "mcb_load")
    private String mcbLoad;

    @Column(name = "islanding_check")
    private String islandingCheck;

    @Column(name = "islanding_satisfactory")
    private String islandingSatisfactory;

    @Column(name = "backup_check")
    private String backupCheck;

    @Column(name = "gen_meter_conn")
    private String genMeterConn;

    @Column(name = "net_meter_conn")
    private String netMeterConn;

    @Column(name = "inverter_healthy")
    private String inverterHealthy;

    @Column(name = "system_takeover")
    private String systemTakeover;

    @Column(name = "net_installed")
    private String netInstalled;

    @Column(name = "net_testing")
    private String netTesting;

    @Column(name = "gen_installed")
    private String genInstalled;

    @Column(name = "gen_testing")
    private String genTesting;

    @ElementCollection
    @CollectionTable(name = "solar_vendor_signatures", joinColumns = @JoinColumn(name = "solar_record_id"))
    @Column(name = "signature", columnDefinition = "TEXT")
    private List<String> vendorSignature = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "solar_consumer_signatures", joinColumns = @JoinColumn(name = "solar_record_id"))
    @Column(name = "signature", columnDefinition = "TEXT")
    private List<String> consumerSignature = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "solar_msedcl_signatures", joinColumns = @JoinColumn(name = "solar_record_id"))
    @Column(name = "signature", columnDefinition = "TEXT")
    private List<String> msedclSignature = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "solar_vendor_stamps", joinColumns = @JoinColumn(name = "solar_record_id"))
    @Column(name = "stamp", columnDefinition = "TEXT")
    private List<String> vendorStamp = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "solar_witness_signatures", joinColumns = @JoinColumn(name = "solar_record_id"))
    @Column(name = "signature", columnDefinition = "TEXT")
    private List<String> witnessSignature = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "solar_site_photos", joinColumns = @JoinColumn(name = "solar_record_id"))
    @Column(name = "photo", columnDefinition = "TEXT")
    private List<String> sitePhotos = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "solar_aadhar_images", joinColumns = @JoinColumn(name = "solar_record_id"))
    @Column(name = "aadhar_image", columnDefinition = "TEXT")
    private List<String> aadharImages = new ArrayList<>();

    @Transient
    private String currentDate;
}