package com.suraj.Customer_Portal_29.controller;

import com.suraj.Customer_Portal_29.dto.request.UserPermissionUpdateDto;
import com.suraj.Customer_Portal_29.dto.request.UserRequestDto;
import com.suraj.Customer_Portal_29.dto.response.ApiResponseDto;
import com.suraj.Customer_Portal_29.dto.response.UserResponseDto;
import com.suraj.Customer_Portal_29.service.UserManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/users")
public class UserManagementController {

    private final UserManagementService service;

    public UserManagementController(UserManagementService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApiResponseDto<UserResponseDto>> createUser(@RequestBody UserRequestDto request) {
        UserResponseDto data = service.createUser(request);
        return ResponseEntity.ok(new ApiResponseDto<>("User created successfully", data));
    }

    @GetMapping
    public ApiResponseDto<java.util.List<UserResponseDto>> getAllUsers() {
        java.util.List<UserResponseDto> data = service.getAllUsers();
        return new ApiResponseDto<>("Users fetched successfully", data);
    }

    @GetMapping("/{userId}")
    public ApiResponseDto<UserResponseDto> getUserById(@PathVariable Long userId) {
        UserResponseDto data = service.getUserById(userId);
        return new ApiResponseDto<>("User fetched successfully", data);
    }

    @PutMapping(value = "/{userId}", consumes = {"multipart/form-data"})
    public ApiResponseDto<UserResponseDto> updateUser(
            @PathVariable Long userId,
            @Valid @ModelAttribute UserRequestDto request,
            @RequestParam(required = false) MultipartFile headerLogo,
            @RequestParam(required = false) MultipartFile vendorSignature,
            @RequestParam(required = false) MultipartFile witness1Signature,
            @RequestParam(required = false) MultipartFile witness2Signature) {
        UserResponseDto data = service.updateUser(userId, request, headerLogo, vendorSignature, witness1Signature, witness2Signature);
        return new ApiResponseDto<>("User updated successfully", data);
    }

    @PutMapping("/{userId}/permissions")
    public ApiResponseDto<UserResponseDto> updatePermissions(
            @PathVariable Long userId,
            @RequestBody UserPermissionUpdateDto request) {
        UserResponseDto data = service.updateUserPermissions(userId, request.getPermissions());
        return new ApiResponseDto<>("Permissions updated successfully", data);
    }

    @PutMapping("/{userId}/status")
    public ApiResponseDto<UserResponseDto> updateStatus(
            @PathVariable Long userId,
            @RequestParam boolean active) {
        UserResponseDto data = service.updateUserStatus(userId, active);
        return new ApiResponseDto<>("User status updated", data);
    }

    @DeleteMapping("/{userId}")
    public ApiResponseDto<String> deleteUser(@PathVariable Long userId) {
        service.deleteUser(userId);
        return new ApiResponseDto<>("User deleted successfully", null);
    }

    @GetMapping("/me")
    public ApiResponseDto<UserResponseDto> getCurrentUserProfile() {
        UserResponseDto data = service.getCurrentUserProfile();
        return new ApiResponseDto<>("User fetched successfully", data);
    }

    @PutMapping(value = "/me", consumes = {"multipart/form-data"})
    public ApiResponseDto<UserResponseDto> updateCurrentUserProfile(
            @Valid @ModelAttribute UserRequestDto request,
            @RequestParam(required = false) MultipartFile headerLogo,
            @RequestParam(required = false) MultipartFile vendorSignature,
            @RequestParam(required = false) MultipartFile witness1Signature,
            @RequestParam(required = false) MultipartFile witness2Signature) {
        UserResponseDto data = service.updateCurrentUserProfile(request, headerLogo, vendorSignature, witness1Signature, witness2Signature);
        return new ApiResponseDto<>("Profile updated successfully", data);
    }
}