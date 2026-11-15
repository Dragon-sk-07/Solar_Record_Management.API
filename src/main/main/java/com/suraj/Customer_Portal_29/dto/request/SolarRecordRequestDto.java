package com.suraj.Customer_Portal_29.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.List;

@Data
public class SolarRecordRequestDto {

    @NotBlank(message = "Consumer name is required")
    @Size(max = 100, message = "Consumer name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Consumer number is required")
    private String consumerNumber;

    @NotBlank(message = "Mobile number is required")
    @Size(min = 10, max = 10, message = "Mobile number must be 10 digits")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Mobile number must be a valid Indian number")
    private String mobileNumber;

    private String email;

    private String aadharNumber;

    private String siteAddress;

    private String place;

    private String applicationNumber;

    private LocalDate applicationDate;

    private String sanctionNumber;

    private String billingUnit;

    private String connectionType;

    private Double sanctionedCapacity;

    private Double installedCapacity;

    private LocalDate installationDate;

    private String moduleMake;

    private Double wattagePerModule;

    private Integer numberOfModules;

    private Double totalCapacityKWP;

    private String moduleSerialNumbers;

    private Integer numberOfStrings;

    private String inverterMake;

    private String inverterModelNumber;

    private Double inverterCapacity;

    private Double totalAmountIncludingGST;

    private List<MultipartFile> consumerSignature;
    private List<MultipartFile> aadharImages;
    private List<MultipartFile> sitePhotos;
    private List<MultipartFile> netMeteringStamp;
    private List<MultipartFile> annexureTwoStamp;

    private List<String> existingConsumerSignature;
    private List<String> existingAadharImages;
    private List<String> existingSitePhotos;
    private List<String> existingNetMeteringStamp;
    private List<String> existingAnnexureTwoStamp;

    private String cellManufacturerName;
    private String cellGSTInvoiceNo;
    private String meterMake;
    private String meterNumber;
    private String netMeterNumber;
    private String invoiceNumber;
    private Integer yearOfManufacturing;
}