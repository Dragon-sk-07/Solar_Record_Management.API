package com.suraj.Customer_Portal_29.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "app_user")
public class Owner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String mobile;

    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_permissions", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "permission")
    private Set<Permission> permissions = new HashSet<>();

    @Column(name = "vendor_address", length = 500)
    private String vendorAddress;

    @Column(name = "authorized_person_name")
    private String authorizedPersonName;

    @Column(name = "witness1_name")
    private String witness1Name;

    @Column(name = "witness1_address", length = 500)
    private String witness1Address;

    @Column(name = "witness2_name")
    private String witness2Name;

    @Column(name = "witness2_address", length = 500)
    private String witness2Address;

    @Column(name = "vendor_mobile")
    private String vendorMobile;

    @Column(name = "vendor_email")
    private String vendorEmail;

    @Column(name = "bank_account_name")
    private String bankAccountName;

    @Column(name = "bank_account_number")
    private String bankAccountNumber;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bank_ifsc_code")
    private String bankIfscCode;

    @Column(name = "branch_name")
    private String branchName;

    @Column(name = "designation")
    private String designation;

    @Column(name = "header_logo_url", length = 1000)
    private String headerLogoUrl;

    @Column(name = "vendor_signature_url", length = 1000)
    private String vendorSignatureUrl;

    @Column(name = "witness1_signature_url", length = 1000)
    private String witness1SignatureUrl;

    @Column(name = "witness2_signature_url", length = 1000)
    private String witness2SignatureUrl;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}