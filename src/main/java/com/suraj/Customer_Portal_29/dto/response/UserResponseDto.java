package com.suraj.Customer_Portal_29.dto.response;

import com.suraj.Customer_Portal_29.entity.Permission;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class UserResponseDto {
    private Long id;
    private String name;
    private String email;
    private String mobile;
    private String role;
    private boolean isActive;
    private Set<Permission> permissions;
    private LocalDateTime createdAt;
    private String vendorAddress;
    private String authorizedPersonName;
    private String witness1Name;
    private String witness1Address;
    private String witness2Name;
    private String witness2Address;
    private String vendorMobile;
    private String vendorEmail;
    private String bankAccountName;
    private String bankAccountNumber;
    private String bankName;
    private String bankIfscCode;
    private String branchName;
    private String designation;
    private String headerLogoUrl;
    private String vendorSignatureUrl;
    private String witness1SignatureUrl;
    private String witness2SignatureUrl;
}