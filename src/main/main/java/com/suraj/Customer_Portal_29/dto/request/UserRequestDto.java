package com.suraj.Customer_Portal_29.dto.request;

import com.suraj.Customer_Portal_29.entity.Permission;
import lombok.Data;
import java.util.Set;

@Data
public class UserRequestDto {
    private Long id;
    private String email;
    private String name;
    private String mobile;
    private String password;
    private Set<Permission> permissions;
    private Boolean isActive;
    private String vendorAddress;
    private String authorizedPersonName;
    private String witness1Name;
    private String witness1Address;
    private String witness2Name;
    private String witness2Address;
    private String bankAccountName;
    private String bankAccountNumber;
    private String bankName;
    private String bankIfscCode;
    private String branchName;
    private String designation;
    private String existingHeaderLogo;
    private String existingVendorSignature;
    private String existingWitness1Signature;
    private String existingWitness2Signature;
    private boolean deleteHeaderLogo;
    private boolean deleteVendorSignature;
    private boolean deleteWitness1Signature;
    private boolean deleteWitness2Signature;
}