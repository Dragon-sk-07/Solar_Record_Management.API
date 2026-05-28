package com.suraj.Customer_Portal_29.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
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
    @Column(nullable = false)
    private boolean isActive = true;
    private LocalDateTime createdAt;
    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_permissions", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "permission")
    private Set<Permission> permissions = new HashSet<>();

    @Column(length = 500)
    private String vendorAddress;
    private String authorizedPersonName;
    private String witness1Name;
    @Column(length = 500)
    private String witness1Address;
    private String witness2Name;
    @Column(length = 500)
    private String witness2Address;
    private String vendorMobile;
    private String vendorEmail;
    private String bankAccountName;
    private String bankAccountNumber;
    private String bankName;
    private String bankIfscCode;
    @Column(length = 1000)
    private String headerLogoUrl;
    @Column(length = 1000)
    private String vendorSignatureUrl;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}