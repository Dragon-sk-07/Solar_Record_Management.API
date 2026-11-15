package com.suraj.Customer_Portal_29.dto.response;

import com.suraj.Customer_Portal_29.entity.Permission;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class LoginResponseDto {
    private String token;
    private String name;
    private String role;
    private Set<Permission> permissions;
}
