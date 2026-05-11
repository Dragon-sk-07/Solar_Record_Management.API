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
public class Owner {  // Keeping name Owner for compatibility, but it's our User

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String mobile;

    private String password;

    // NEW: Role - either SUPER_ADMIN or USER
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER;

    // NEW: Account active status
    @Column(nullable = false)
    private boolean isActive = true;

    // NEW: When user was created
    private LocalDateTime createdAt;

    // NEW: Permissions assigned by Super Admin (only for USER role)
    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_permissions", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "permission")
    private Set<Permission> permissions = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}