package com.suraj.Customer_Portal_29.controller;

import com.suraj.Customer_Portal_29.dto.request.UserCreateRequestDto;
import com.suraj.Customer_Portal_29.dto.request.UserPermissionUpdateDto;
import com.suraj.Customer_Portal_29.dto.response.ApiResponseDto;
import com.suraj.Customer_Portal_29.dto.response.UserResponseDto;
import com.suraj.Customer_Portal_29.entity.Owner;
import com.suraj.Customer_Portal_29.entity.Permission;
import com.suraj.Customer_Portal_29.service.UserManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;
import com.suraj.Customer_Portal_29.entity.UserRole;
import com.suraj.Customer_Portal_29.repository.OwnerRepository;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/admin/users")
public class UserManagementController {

    private final UserManagementService userManagementService;

    private final OwnerRepository ownerRepository;

    public UserManagementController(UserManagementService userManagementService, OwnerRepository ownerRepository) {
        this.userManagementService = userManagementService;
        this.ownerRepository = ownerRepository;
    }
    private void checkSuperAdmin() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Owner currentUser = ownerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (currentUser.getRole() != UserRole.SUPER_ADMIN) {
            throw new RuntimeException("Access denied. Super admin only.");
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponseDto<UserResponseDto>> createUser(@RequestBody UserCreateRequestDto request) {
        checkSuperAdmin();
        Owner user = userManagementService.createUser(
                request.getEmail(),
                request.getName(),
                request.getMobile(),
                request.getPassword(),
                request.getPermissions()
        );
        return ResponseEntity.ok(new ApiResponseDto<>("User created successfully", mapToResponse(user)));
    }

    @PutMapping("/{userId}/permissions")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> updatePermissions(
            @PathVariable Long userId,
            @RequestBody UserPermissionUpdateDto request) {
        checkSuperAdmin();
        Owner user = userManagementService.updateUserPermissions(userId, request.getPermissions());
        return ResponseEntity.ok(new ApiResponseDto<>("Permissions updated successfully", mapToResponse(user)));
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> updateStatus(
            @PathVariable Long userId,
            @RequestParam boolean active) {
        checkSuperAdmin();
        Owner user = userManagementService.updateUserStatus(userId, active);
        return ResponseEntity.ok(new ApiResponseDto<>("User status updated", mapToResponse(user)));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<UserResponseDto>>> getAllUsers() {
        checkSuperAdmin();
        List<UserResponseDto> users = userManagementService.getAllUsers().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponseDto<>("Users fetched successfully", users));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteUser(@PathVariable Long userId) {
        checkSuperAdmin();
        userManagementService.deleteUser(userId);
        return ResponseEntity.ok(new ApiResponseDto<>("User deleted successfully", null));
    }

    private UserResponseDto mapToResponse(Owner user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setMobile(user.getMobile());
        dto.setRole(user.getRole().name());
        dto.setActive(user.isActive());
        dto.setPermissions(user.getPermissions());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}