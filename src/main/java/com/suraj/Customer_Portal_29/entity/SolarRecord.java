package com.suraj.Customer_Portal_29.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    // ==================== BASIC INFORMATION ====================
    private String name;
    private String consumerNumber;
    private String mobileNumber;
    private String email;

    @Column(length = 1000)
    private String siteAddress;

    private String category;

    @Column(name = "aadhar_number")
    private String aadharNumber;

    // ==================== SANCTION DETAILS ====================
    @Column(name = "sanction_number")
    private String sanctionNumber;

    @Column(name = "sanctioned_capacity")
    private Double sanctionedCapacity;

    @Column(name = "installed_capacity")
    private Double installedCapacity;

    // ==================== RE ARRANGEMENT ====================
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

    // ==================== MODULE SPECIFICATIONS ====================
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

    // ==================== WARRANTY DETAILS ====================
    @Column(name = "product_warranty", length = 1000)
    private String productWarranty;

    @Column(name = "performance_warranty", length = 1000)
    private String performanceWarranty;

    // ==================== INVERTER DETAILS ====================
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

    // ==================== EARTHING AND PROTECTIONS ====================
    @Column(name = "number_of_earthings")
    private Integer numberOfEarthings;

    @Column(name = "earth_resistance")
    private Double earthResistance;

    @Column(name = "lightening_arrester")
    private String lighteningArrester;

    // ==================== VENDOR DETAILS ====================
    @Column(name = "vendor_name")
    private String vendorName;

    @Column(name = "vendor_stamp", length = 500)
    private String vendorStamp;

    @Column(name = "vendor_address", length = 1000)
    private String vendorAddress;

    @Column(name = "authorized_person_name")
    private String authorizedPersonName;

    @Column(name = "designation")
    private String designation;

    // ==================== MSEDCL & INSPECTOR DETAILS ====================
    @Column(name = "msedcl_address", length = 1000)
    private String msedclAddress;

    @Column(name = "msedcl_officer_name")
    private String msedclOfficerName;

    @Column(name = "msedcl_officer_designation")
    private String msedclOfficerDesignation;

    @Column(name = "inspector_name")
    private String inspectorName;

    // ==================== AGREEMENT DETAILS ====================
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

    // ==================== APPLICATION DETAILS ====================
    @Column(name = "application_number")
    private String applicationNumber;

    @Column(name = "application_date")
    private LocalDate applicationDate;

    @Column(name = "discom_name")
    private String discomName;

    @Column(name = "place")
    private String place;

    // ==================== WITNESS DETAILS ====================
    @Column(name = "witness1_name")
    private String witness1Name;

    @Column(name = "witness1_address", length = 1000)
    private String witness1Address;

    @Column(name = "witness2_name")
    private String witness2Name;

    @Column(name = "witness2_address", length = 1000)
    private String witness2Address;

    // ==================== PHOTOS ====================
    @ElementCollection
    @CollectionTable(name = "solar_site_photos", joinColumns = @JoinColumn(name = "solar_record_id"))
    @Column(name = "photo", columnDefinition = "TEXT")
    private List<String> sitePhotos;

    @ElementCollection
    @CollectionTable(name = "solar_aadhar_images", joinColumns = @JoinColumn(name = "solar_record_id"))
    @Column(name = "aadhar_image", columnDefinition = "TEXT")
    private List<String> aadharImages;
}