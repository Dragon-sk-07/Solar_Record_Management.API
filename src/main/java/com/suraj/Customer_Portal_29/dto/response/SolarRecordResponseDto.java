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
    private String aadharNumber;
    private String siteAddress;
    private String place;
    private String applicationNumber;
    private String applicationDate;
    private String sanctionNumber;
    private String billingUnit;
    private String connectionType;

    private Double sanctionedCapacity;
    private Double installedCapacity;
    private String installationDate;

    private String moduleMake;
    private String almmModelNumber;
    private Double wattagePerModule;
    private Integer numberOfModules;
    private Double totalCapacityKWP;
    private String moduleSerialNumbers;
    private Integer numberOfStrings;

    private String inverterMake;
    private String inverterModelNumber;
    private Double inverterCapacity;
    private String lighteningArrester;

    private String vendorName;
    private String vendorAddress;
    private String vendorMobile;
    private String vendorEmail;
    private String authorizedPersonName;
    private String msedclOfficerName;

    private String witness1Name;
    private String witness1Address;
    private String witness2Name;
    private String witness2Address;

    private List<String> vendorSignature;
    private List<String> consumerSignature;
    private List<String> msedclSignature;
    private List<String> vendorStamp;
    private List<String> witnessSignature;
    private List<String> aadharImages;
    private List<String> sitePhotos;
    private List<String> netMeteringStamp;
    private List<String> annexureTwoStamp;
}