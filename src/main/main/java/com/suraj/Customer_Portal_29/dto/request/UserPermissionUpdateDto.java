package com.suraj.Customer_Portal_29.dto.request;

import com.suraj.Customer_Portal_29.entity.Permission;
import lombok.Data;
import java.util.Set;

@Data
public class UserPermissionUpdateDto {
    private Set<Permission> permissions;
}