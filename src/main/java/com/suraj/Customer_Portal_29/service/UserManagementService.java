package com.suraj.Customer_Portal_29.service;

import com.suraj.Customer_Portal_29.dto.request.UserRequestDto;
import com.suraj.Customer_Portal_29.entity.Owner;
import com.suraj.Customer_Portal_29.entity.Permission;
import com.suraj.Customer_Portal_29.entity.SolarRecord;
import com.suraj.Customer_Portal_29.entity.UserRole;
import com.suraj.Customer_Portal_29.repository.OwnerRepository;
import com.suraj.Customer_Portal_29.repository.SolarRecordRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

@Service
public class UserManagementService {
    private final OwnerRepository ownerRepository;
    private final PasswordEncoder passwordEncoder;
    private final SolarRecordRepository solarRecordRepository;
    private final CloudinaryService cloudinaryService;

    public UserManagementService(OwnerRepository ownerRepository, PasswordEncoder passwordEncoder, SolarRecordRepository solarRecordRepository, CloudinaryService cloudinaryService) {
        this.ownerRepository = ownerRepository;
        this.passwordEncoder = passwordEncoder;
        this.solarRecordRepository = solarRecordRepository;
        this.cloudinaryService = cloudinaryService;
    }

    public Owner createUser(UserRequestDto request) {
        if (ownerRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("User already exists");
        }
        Owner user = new Owner();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setMobile(request.getMobile());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.USER);
        user.setActive(true);
        user.setPermissions(request.getPermissions() != null && !request.getPermissions().isEmpty() ? request.getPermissions() : Set.of(Permission.VIEW_RECORDS));
        return ownerRepository.save(user);
    }

    public Owner updateUser(Long userId, UserRequestDto request, MultipartFile headerLogo, MultipartFile vendorSignature, MultipartFile witness1Signature, MultipartFile witness2Signature) {
        Owner user = ownerRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        if (request.getName() != null) user.setName(request.getName());
        if (request.getVendorAddress() != null) user.setVendorAddress(request.getVendorAddress());
        if (request.getAuthorizedPersonName() != null) user.setAuthorizedPersonName(request.getAuthorizedPersonName());
        if (request.getWitness1Name() != null) user.setWitness1Name(request.getWitness1Name());
        if (request.getWitness1Address() != null) user.setWitness1Address(request.getWitness1Address());
        if (request.getWitness2Name() != null) user.setWitness2Name(request.getWitness2Name());
        if (request.getWitness2Address() != null) user.setWitness2Address(request.getWitness2Address());
        if (request.getVendorMobile() != null) user.setVendorMobile(request.getVendorMobile());
        if (request.getVendorEmail() != null) user.setVendorEmail(request.getVendorEmail());
        if (request.getBankAccountName() != null) user.setBankAccountName(request.getBankAccountName());
        if (request.getBankAccountNumber() != null) user.setBankAccountNumber(request.getBankAccountNumber());
        if (request.getBankName() != null) user.setBankName(request.getBankName());
        if (request.getBankIfscCode() != null) user.setBankIfscCode(request.getBankIfscCode());
        if (request.getBranchName() != null) user.setBranchName(request.getBranchName());
        if (request.getDesignation() != null) user.setDesignation(request.getDesignation());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) user.setPassword(passwordEncoder.encode(request.getPassword()));
        if (request.getIsActive() != null) user.setActive(request.getIsActive());
        if (request.getPermissions() != null && user.getRole() != UserRole.SUPER_ADMIN) user.setPermissions(request.getPermissions());

        user.setHeaderLogoUrl(syncImage(user.getHeaderLogoUrl(), headerLogo, request.getExistingHeaderLogo(), "userHeaderLogos"));
        user.setVendorSignatureUrl(syncImage(user.getVendorSignatureUrl(), vendorSignature, request.getExistingVendorSignature(), "userVendorSignatures"));
        user.setWitness1SignatureUrl(syncImage(user.getWitness1SignatureUrl(), witness1Signature, request.getExistingWitness1Signature(), "userWitnessSignatures"));
        user.setWitness2SignatureUrl(syncImage(user.getWitness2SignatureUrl(), witness2Signature, request.getExistingWitness2Signature(), "userWitnessSignatures"));

        return ownerRepository.save(user);
    }

    private String syncImage(String existingUrl, MultipartFile newFile, String existingUrlFromRequest, String folder) {
        String finalUrl = null;
        if (existingUrlFromRequest != null && !existingUrlFromRequest.isEmpty()) {
            finalUrl = existingUrlFromRequest;
        }
        if (newFile != null && !newFile.isEmpty()) {
            if (existingUrl != null && !existingUrl.equals(finalUrl)) {
                cloudinaryService.deleteFile(existingUrl);
            }
            finalUrl = cloudinaryService.uploadFile(newFile, folder);
        }
        return finalUrl;
    }

    public Owner updateUserPermissions(Long userId, Set<Permission> permissions) {
        Owner user = ownerRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getRole() == UserRole.SUPER_ADMIN) throw new RuntimeException("Cannot modify SUPER_ADMIN permissions");
        user.setPermissions(permissions);
        return ownerRepository.save(user);
    }

    public Owner updateUserStatus(Long userId, boolean isActive) {
        Owner user = ownerRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(isActive);
        return ownerRepository.save(user);
    }

    public List<Owner> getAllUsers() {
        return ownerRepository.findAll();
    }

    public Owner getUserById(Long userId) {
        return ownerRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void deleteUser(Long userId) {
        Owner user = ownerRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getRole() == UserRole.SUPER_ADMIN) throw new RuntimeException("Cannot delete SUPER_ADMIN");
        if (user.getHeaderLogoUrl() != null) cloudinaryService.deleteFile(user.getHeaderLogoUrl());
        if (user.getVendorSignatureUrl() != null) cloudinaryService.deleteFile(user.getVendorSignatureUrl());
        if (user.getWitness1SignatureUrl() != null) cloudinaryService.deleteFile(user.getWitness1SignatureUrl());
        if (user.getWitness2SignatureUrl() != null) cloudinaryService.deleteFile(user.getWitness2SignatureUrl());
        List<SolarRecord> userRecords = solarRecordRepository.findByCreatedByUserEmail(user.getEmail());
        if (!userRecords.isEmpty()) solarRecordRepository.deleteAll(userRecords);
        ownerRepository.delete(user);
    }
}