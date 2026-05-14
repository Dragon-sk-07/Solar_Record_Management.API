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

    // Customer & Site Information
    private String name;
    @Column(unique = true, nullable = false)
    private String consumerNumber;
    private String mobileNumber;
    private String email;
    @Column(name = "aadhar_number")
    private String aadharNumber;
    @Column(length = 1000)
    private String siteAddress;
    private String place;
    @Column(name = "application_number")
    private String applicationNumber;
    @Column(name = "application_date")
    private LocalDate applicationDate;
    @Column(name = "sanction_number")
    private String sanctionNumber;
    @Column(name = "billing_unit")
    private String billingUnit;
    @Column(name = "connection_type")
    private String connectionType;

    // Sanction Details
    @Column(name = "sanctioned_capacity")
    private Double sanctionedCapacity;
    @NotNull
    @DecimalMin("0.1")
    private Double installedCapacity;
    @Column(name = "installation_date")
    private LocalDate installationDate;

    // Module Details
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
    @Column(name = "number_of_strings")
    private Integer numberOfStrings;

    // Inverter Details
    @Column(name = "inverter_make")
    private String inverterMake;
    @Column(name = "inverter_model_number")
    private String inverterModelNumber;
    @Column(name = "inverter_capacity")
    private Double inverterCapacity;
    @Column(name = "lightening_arrester")
    private String lighteningArrester;

    // Vendor & Witness Information
    @Column(name = "vendor_name")
    private String vendorName;
    @Column(name = "vendor_address", length = 1000)
    private String vendorAddress;
    @Column(name = "vendor_mobile")
    private String vendorMobile;
    @Column(name = "vendor_email")
    private String vendorEmail;
    @Column(name = "authorized_person_name")
    private String authorizedPersonName;
    @Column(name = "msedcl_officer_name")
    private String msedclOfficerName;
    @Column(name = "witness1_name")
    private String witness1Name;
    @Column(name = "witness1_address", length = 1000)
    private String witness1Address;
    @Column(name = "witness2_name")
    private String witness2Name;
    @Column(name = "witness2_address", length = 1000)
    private String witness2Address;

    // File Uploads
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
    @CollectionTable(name = "solar_aadhar_images", joinColumns = @JoinColumn(name = "solar_record_id"))
    @Column(name = "aadhar_image", columnDefinition = "TEXT")
    private List<String> aadharImages = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "solar_site_photos", joinColumns = @JoinColumn(name = "solar_record_id"))
    @Column(name = "photo", columnDefinition = "TEXT")
    private List<String> sitePhotos = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "solar_net_metering_stamps", joinColumns = @JoinColumn(name = "solar_record_id"))
    @Column(name = "stamp", columnDefinition = "TEXT")
    private List<String> netMeteringStamp = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "solar_annexure_two_stamps", joinColumns = @JoinColumn(name = "solar_record_id"))
    @Column(name = "stamp", columnDefinition = "TEXT")
    private List<String> annexureTwoStamp = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private Owner createdBy;

    @Column(name = "created_by_user_email")
    private String createdByUserEmail;

    @Transient
    private String currentDate;
}