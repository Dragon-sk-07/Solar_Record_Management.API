package com.suraj.Customer_Portal_29.controller;

import com.suraj.Customer_Portal_29.dto.request.UserPermissionUpdateDto;
import com.suraj.Customer_Portal_29.dto.request.UserRequestDto;
import com.suraj.Customer_Portal_29.dto.response.ApiResponseDto;
import com.suraj.Customer_Portal_29.dto.response.UserResponseDto;
import com.suraj.Customer_Portal_29.entity.Owner;
import com.suraj.Customer_Portal_29.entity.UserRole;
import com.suraj.Customer_Portal_29.repository.OwnerRepository;
import com.suraj.Customer_Portal_29.service.UserManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.stream.Collectors;

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

    private Owner getLoggedInUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ownerRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDto<UserResponseDto>> createUser(@RequestBody UserRequestDto request) {
        checkSuperAdmin();
        Owner user = userManagementService.createUser(request);
        return ResponseEntity.ok(new ApiResponseDto<>("User created successfully", mapToResponse(user)));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<UserResponseDto>>> getAllUsers() {
        checkSuperAdmin();
        List<UserResponseDto> users = userManagementService.getAllUsers().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponseDto<>("Users fetched successfully", users));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> getUserById(@PathVariable Long userId) {
        checkSuperAdmin();
        Owner user = userManagementService.getUserById(userId);
        return ResponseEntity.ok(new ApiResponseDto<>("User fetched successfully", mapToResponse(user)));
    }

    @PutMapping(value = "/{userId}", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponseDto<UserResponseDto>> updateUser(
            @PathVariable Long userId,
            @ModelAttribute UserRequestDto request,
            @RequestParam(required = false) MultipartFile headerLogo,
            @RequestParam(required = false) MultipartFile vendorSignature,
            @RequestParam(required = false) MultipartFile witness1Signature,
            @RequestParam(required = false) MultipartFile witness2Signature) {
        checkSuperAdmin();
        Owner user = userManagementService.updateUser(userId, request, headerLogo, vendorSignature, witness1Signature, witness2Signature);
        return ResponseEntity.ok(new ApiResponseDto<>("User updated successfully", mapToResponse(user)));
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

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteUser(@PathVariable Long userId) {
        checkSuperAdmin();
        userManagementService.deleteUser(userId);
        return ResponseEntity.ok(new ApiResponseDto<>("User deleted successfully", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> getCurrentUserProfile() {
        Owner currentUser = getLoggedInUser();
        return ResponseEntity.ok(new ApiResponseDto<>("User fetched successfully", mapToResponse(currentUser)));
    }

    @PutMapping(value = "/me", consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponseDto<UserResponseDto>> updateCurrentUserProfile(
            @ModelAttribute UserRequestDto request,
            @RequestParam(required = false) MultipartFile headerLogo,
            @RequestParam(required = false) MultipartFile vendorSignature,
            @RequestParam(required = false) MultipartFile witness1Signature,
            @RequestParam(required = false) MultipartFile witness2Signature) {
        Owner currentUser = getLoggedInUser();
        Owner user = userManagementService.updateUser(currentUser.getId(), request, headerLogo, vendorSignature, witness1Signature, witness2Signature);
        return ResponseEntity.ok(new ApiResponseDto<>("Profile updated successfully", mapToResponse(user)));
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
        dto.setVendorAddress(user.getVendorAddress());
        dto.setAuthorizedPersonName(user.getAuthorizedPersonName());
        dto.setWitness1Name(user.getWitness1Name());
        dto.setWitness1Address(user.getWitness1Address());
        dto.setWitness2Name(user.getWitness2Name());
        dto.setWitness2Address(user.getWitness2Address());
        dto.setVendorMobile(user.getVendorMobile());
        dto.setVendorEmail(user.getVendorEmail());
        dto.setBankAccountName(user.getBankAccountName());
        dto.setBankAccountNumber(user.getBankAccountNumber());
        dto.setBankName(user.getBankName());
        dto.setBankIfscCode(user.getBankIfscCode());
        dto.setBranchName(user.getBranchName());
        dto.setDesignation(user.getDesignation());
        dto.setHeaderLogoUrl(user.getHeaderLogoUrl());
        dto.setVendorSignatureUrl(user.getVendorSignatureUrl());
        dto.setWitness1SignatureUrl(user.getWitness1SignatureUrl());
        dto.setWitness2SignatureUrl(user.getWitness2SignatureUrl());
        return dto;
    }
}