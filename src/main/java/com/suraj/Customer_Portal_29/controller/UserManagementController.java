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

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponseDto<UserResponseDto>> createUser(
            @Valid @ModelAttribute UserRequestDto request,
            @RequestParam(required = false) MultipartFile headerLogo,
            @RequestParam(required = false) MultipartFile vendorSignature,
            @RequestParam(required = false) MultipartFile witness1Signature,
            @RequestParam(required = false) MultipartFile witness2Signature) {
        UserResponseDto data = service.createUser(request, headerLogo, vendorSignature, witness1Signature, witness2Signature);
        return ResponseEntity.ok(new ApiResponseDto<>("User created successfully", data));
    }

    @PutMapping(value = "/{userId}", consumes = {"multipart/form-data"})
    public ApiResponseDto<UserResponseDto> updateUser(
            @PathVariable Long userId,
            @Valid @ModelAttribute UserRequestDto request,
            @RequestParam(required = false) MultipartFile headerLogo,
            @RequestParam(required = false) MultipartFile vendorSignature,
            @RequestParam(required = false) MultipartFile witness1Signature,
            @RequestParam(required = false) MultipartFile witness2Signature,
            @RequestParam(required = false) String existingHeaderLogo,
            @RequestParam(required = false) String existingVendorSignature,
            @RequestParam(required = false) String existingWitness1Signature,
            @RequestParam(required = false) String existingWitness2Signature,
            @RequestParam(required = false) boolean deleteHeaderLogo,
            @RequestParam(required = false) boolean deleteVendorSignature,
            @RequestParam(required = false) boolean deleteWitness1Signature,
            @RequestParam(required = false) boolean deleteWitness2Signature) {

        request.setExistingHeaderLogo(existingHeaderLogo);
        request.setExistingVendorSignature(existingVendorSignature);
        request.setExistingWitness1Signature(existingWitness1Signature);
        request.setExistingWitness2Signature(existingWitness2Signature);
        request.setDeleteHeaderLogo(deleteHeaderLogo);
        request.setDeleteVendorSignature(deleteVendorSignature);
        request.setDeleteWitness1Signature(deleteWitness1Signature);
        request.setDeleteWitness2Signature(deleteWitness2Signature);

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
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String mobile,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String vendorAddress,
            @RequestParam(required = false) String authorizedPersonName,
            @RequestParam(required = false) String witness1Name,
            @RequestParam(required = false) String witness1Address,
            @RequestParam(required = false) String witness2Name,
            @RequestParam(required = false) String witness2Address,
            @RequestParam(required = false) String designation,
            @RequestParam(required = false) String bankAccountName,
            @RequestParam(required = false) String bankAccountNumber,
            @RequestParam(required = false) String bankName,
            @RequestParam(required = false) String bankIfscCode,
            @RequestParam(required = false) String branchName,
            @RequestParam(required = false) MultipartFile headerLogo,
            @RequestParam(required = false) MultipartFile vendorSignature,
            @RequestParam(required = false) MultipartFile witness1Signature,
            @RequestParam(required = false) MultipartFile witness2Signature,
            @RequestParam(required = false) String existingHeaderLogo,
            @RequestParam(required = false) String existingVendorSignature,
            @RequestParam(required = false) String existingWitness1Signature,
            @RequestParam(required = false) String existingWitness2Signature,
            @RequestParam(required = false) boolean deleteHeaderLogo,
            @RequestParam(required = false) boolean deleteVendorSignature,
            @RequestParam(required = false) boolean deleteWitness1Signature,
            @RequestParam(required = false) boolean deleteWitness2Signature) {

        UserRequestDto request = new UserRequestDto();
        request.setName(name);
        request.setEmail(email);
        request.setMobile(mobile);
        request.setPassword(password);
        request.setVendorAddress(vendorAddress);
        request.setAuthorizedPersonName(authorizedPersonName);
        request.setWitness1Name(witness1Name);
        request.setWitness1Address(witness1Address);
        request.setWitness2Name(witness2Name);
        request.setWitness2Address(witness2Address);
        request.setDesignation(designation);
        request.setBankAccountName(bankAccountName);
        request.setBankAccountNumber(bankAccountNumber);
        request.setBankName(bankName);
        request.setBankIfscCode(bankIfscCode);
        request.setBranchName(branchName);
        request.setExistingHeaderLogo(existingHeaderLogo);
        request.setExistingVendorSignature(existingVendorSignature);
        request.setExistingWitness1Signature(existingWitness1Signature);
        request.setExistingWitness2Signature(existingWitness2Signature);
        request.setDeleteHeaderLogo(deleteHeaderLogo);
        request.setDeleteVendorSignature(deleteVendorSignature);
        request.setDeleteWitness1Signature(deleteWitness1Signature);
        request.setDeleteWitness2Signature(deleteWitness2Signature);

        UserResponseDto data = service.updateCurrentUserProfile(request, headerLogo, vendorSignature, witness1Signature, witness2Signature);
        return new ApiResponseDto<>("Profile updated successfully", data);
    }
}