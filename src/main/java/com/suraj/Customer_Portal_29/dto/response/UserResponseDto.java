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
}