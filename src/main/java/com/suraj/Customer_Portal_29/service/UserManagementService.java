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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserManagementService {

    private final OwnerRepository ownerRepository;
    private final PasswordEncoder passwordEncoder;
    private final SolarRecordRepository solarRecordRepository;
    private final CloudinaryService cloudinaryService;

    public UserManagementService(OwnerRepository ownerRepository,
                                 PasswordEncoder passwordEncoder,
                                 SolarRecordRepository solarRecordRepository,
                                 CloudinaryService cloudinaryService) {
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

        if (request.getPermissions() == null || request.getPermissions().isEmpty()) {
            user.setPermissions(Set.of(Permission.VIEW_RECORDS));
        } else {
            user.setPermissions(request.getPermissions());
        }
        return ownerRepository.save(user);
    }

    public Owner updateUser(Long userId,
                            UserRequestDto request,
                            MultipartFile headerLogo,
                            MultipartFile vendorSignature,
                            MultipartFile witness1Signature,
                            MultipartFile witness2Signature) {

        System.out.println("=== UserManagementService.updateUser ===");
        System.out.println("User ID: " + userId);

        Owner user = ownerRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        // Update text fields
        if (request.getVendorAddress() != null) {
            user.setVendorAddress(request.getVendorAddress());
            System.out.println("Set vendorAddress: " + request.getVendorAddress());
        }
        if (request.getAuthorizedPersonName() != null) {
            user.setAuthorizedPersonName(request.getAuthorizedPersonName());
            System.out.println("Set authorizedPersonName: " + request.getAuthorizedPersonName());
        }
        if (request.getWitness1Name() != null) {
            user.setWitness1Name(request.getWitness1Name());
            System.out.println("Set witness1Name: " + request.getWitness1Name());
        }
        if (request.getWitness1Address() != null) {
            user.setWitness1Address(request.getWitness1Address());
            System.out.println("Set witness1Address: " + request.getWitness1Address());
        }
        if (request.getWitness2Name() != null) {
            user.setWitness2Name(request.getWitness2Name());
            System.out.println("Set witness2Name: " + request.getWitness2Name());
        }
        if (request.getWitness2Address() != null) {
            user.setWitness2Address(request.getWitness2Address());
            System.out.println("Set witness2Address: " + request.getWitness2Address());
        }
        if (request.getVendorMobile() != null) {
            user.setVendorMobile(request.getVendorMobile());
            System.out.println("Set vendorMobile: " + request.getVendorMobile());
        }
        if (request.getVendorEmail() != null) {
            user.setVendorEmail(request.getVendorEmail());
            System.out.println("Set vendorEmail: " + request.getVendorEmail());
        }
        if (request.getBankAccountName() != null) {
            user.setBankAccountName(request.getBankAccountName());
            System.out.println("Set bankAccountName: " + request.getBankAccountName());
        }
        if (request.getBankAccountNumber() != null) {
            user.setBankAccountNumber(request.getBankAccountNumber());
            System.out.println("Set bankAccountNumber: " + request.getBankAccountNumber());
        }
        if (request.getBankName() != null) {
            user.setBankName(request.getBankName());
            System.out.println("Set bankName: " + request.getBankName());
        }
        if (request.getBankIfscCode() != null) {
            user.setBankIfscCode(request.getBankIfscCode());
            System.out.println("Set bankIfscCode: " + request.getBankIfscCode());
        }
        if (request.getBranchName() != null) {
            user.setBranchName(request.getBranchName());
            System.out.println("Set branchName: " + request.getBranchName());
        }
        if (request.getDesignation() != null) {
            user.setDesignation(request.getDesignation());
            System.out.println("Set designation: " + request.getDesignation());
        }
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            System.out.println("Password updated");
        }

        // Update images using syncImage pattern (same as SolarRecordService)
        user.setHeaderLogoUrl(syncImage(user.getHeaderLogoUrl(), headerLogo, request.getExistingHeaderLogo(), "userHeaderLogos"));
        user.setVendorSignatureUrl(syncImage(user.getVendorSignatureUrl(), vendorSignature, request.getExistingVendorSignature(), "userVendorSignatures"));
        user.setWitness1SignatureUrl(syncImage(user.getWitness1SignatureUrl(), witness1Signature, request.getExistingWitness1Signature(), "userWitnessSignatures"));
        user.setWitness2SignatureUrl(syncImage(user.getWitness2SignatureUrl(), witness2Signature, request.getExistingWitness2Signature(), "userWitnessSignatures"));

        Owner savedUser = ownerRepository.saveAndFlush(user);

        System.out.println("User saved successfully. VendorAddress: " + savedUser.getVendorAddress());
        System.out.println("Witness1Name: " + savedUser.getWitness1Name());

        return savedUser;
    }

    // Same syncImage logic as SolarRecordService
    private String syncImage(String existingUrl, MultipartFile newFile, String existingUrlFromRequest, String folder) {
        String finalUrl = null;

        // If existing URL from request is provided, use it (keeps existing image)
        if (existingUrlFromRequest != null && !existingUrlFromRequest.isEmpty()) {
            finalUrl = existingUrlFromRequest;
            System.out.println("Keeping existing image: " + finalUrl);
        }

        // If new file is uploaded, upload new one and delete old
        if (newFile != null && !newFile.isEmpty()) {
            System.out.println("Uploading new image to folder: " + folder);
            if (existingUrl != null && !existingUrl.equals(finalUrl)) {
                cloudinaryService.deleteFile(existingUrl);
                System.out.println("Deleted old image: " + existingUrl);
            }
            finalUrl = cloudinaryService.uploadFile(newFile, folder);
            System.out.println("Uploaded new image: " + finalUrl);
        }

        return finalUrl;
    }

    // Same uploadImagesWithCompression logic as SolarRecordService
    private List<String> uploadImagesWithCompression(List<MultipartFile> files, String folder) {
        if (files == null || files.isEmpty()) return Collections.emptyList();
        return files.stream()
                .filter(Objects::nonNull)
                .map(file -> cloudinaryService.uploadFile(file, folder))
                .collect(Collectors.toList());
    }

    // Same syncImages logic as SolarRecordService (for multiple images if needed)
    private List<String> syncImages(List<String> existingImages, List<MultipartFile> newFiles, List<String> existingUrlsFromRequest, String folder) {
        List<String> finalUrls = new ArrayList<>();
        if (existingUrlsFromRequest != null) finalUrls.addAll(existingUrlsFromRequest);
        if (newFiles != null && !newFiles.isEmpty()) finalUrls.addAll(uploadImagesWithCompression(newFiles, folder));
        if (existingImages != null && !existingImages.isEmpty()) {
            List<String> urlsToDelete = existingImages.stream().filter(url -> !finalUrls.contains(url)).collect(Collectors.toList());
            if (!urlsToDelete.isEmpty()) cloudinaryService.deleteFiles(urlsToDelete);
        }
        return finalUrls;
    }

    public Owner updateUserPermissions(Long userId, Set<Permission> permissions) {
        Owner user = ownerRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getRole() == UserRole.SUPER_ADMIN) {
            throw new RuntimeException("Cannot modify SUPER_ADMIN permissions");
        }
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
        if (user.getRole() == UserRole.SUPER_ADMIN) {
            throw new RuntimeException("Cannot delete SUPER_ADMIN");
        }

        if (user.getHeaderLogoUrl() != null) cloudinaryService.deleteFile(user.getHeaderLogoUrl());
        if (user.getVendorSignatureUrl() != null) cloudinaryService.deleteFile(user.getVendorSignatureUrl());
        if (user.getWitness1SignatureUrl() != null) cloudinaryService.deleteFile(user.getWitness1SignatureUrl());
        if (user.getWitness2SignatureUrl() != null) cloudinaryService.deleteFile(user.getWitness2SignatureUrl());

        List<SolarRecord> userRecords = solarRecordRepository.findByCreatedByUserEmail(user.getEmail());
        if (!userRecords.isEmpty()) {
            solarRecordRepository.deleteAll(userRecords);
        }

        ownerRepository.delete(user);
    }
}