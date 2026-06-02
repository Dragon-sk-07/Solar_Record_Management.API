package com.suraj.Customer_Portal_29.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
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

    @Column(name = "sanctioned_capacity")
    private Double sanctionedCapacity;

    private Double installedCapacity;

    @Column(name = "installation_date")
    private LocalDate installationDate;

    @Column(name = "module_make")
    private String moduleMake;

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

    @Column(name = "inverter_make")
    private String inverterMake;

    @Column(name = "inverter_model_number")
    private String inverterModelNumber;

    @Column(name = "inverter_capacity")
    private Double inverterCapacity;

    @Column(name = "total_amount_including_gst")
    private Double totalAmountIncludingGST;

    @Column(name = "cell_manufacturer_name")
    private String cellManufacturerName;

    @Column(name = "cell_gst_invoice_no")
    private String cellGSTInvoiceNo;

    @Column(name = "meter_make")
    private String meterMake;

    @Column(name = "meter_number")
    private String meterNumber;

    @Column(name = "net_meter_number")
    private String netMeterNumber;

    @Column(name = "invoice_number")
    private String invoiceNumber;

    @Column(name = "year_of_manufacturing")
    private Integer yearOfManufacturing;

    @ElementCollection
    @CollectionTable(name = "solar_consumer_signatures", joinColumns = @JoinColumn(name = "solar_record_id"))
    @Column(name = "signature", columnDefinition = "TEXT")
    private List<String> consumerSignature = new ArrayList<>();

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