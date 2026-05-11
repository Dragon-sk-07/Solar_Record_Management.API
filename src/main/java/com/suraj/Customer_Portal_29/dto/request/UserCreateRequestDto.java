package com.suraj.Customer_Portal_29.dto.request;

import com.suraj.Customer_Portal_29.entity.Permission;
import lombok.Data;
import java.util.Set;

@Data
public class UserCreateRequestDto {
    private String email;
    private String name;
    private String mobile;
    private String password;
    private Set<Permission> permissions;
}