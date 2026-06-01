package com.suraj.Customer_Portal_29.service;

import com.suraj.Customer_Portal_29.dto.request.UserRequestDto;
import com.suraj.Customer_Portal_29.dto.response.UserResponseDto;
import com.suraj.Customer_Portal_29.entity.Owner;
import com.suraj.Customer_Portal_29.entity.Permission;
import com.suraj.Customer_Portal_29.entity.SolarRecord;
import com.suraj.Customer_Portal_29.entity.UserRole;
import com.suraj.Customer_Portal_29.repository.OwnerRepository;
import com.suraj.Customer_Portal_29.repository.SolarRecordRepository;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final ModelMapper modelMapper;

    public UserManagementService(OwnerRepository ownerRepository,
                                 PasswordEncoder passwordEncoder,
                                 SolarRecordRepository solarRecordRepository,
                                 CloudinaryService cloudinaryService,
                                 ModelMapper modelMapper) {
        this.ownerRepository = ownerRepository;
        this.passwordEncoder = passwordEncoder;
        this.solarRecordRepository = solarRecordRepository;
        this.cloudinaryService = cloudinaryService;
        this.modelMapper = modelMapper;
    }

    private Owner getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("[DEBUG] getCurrentUser - Email from SecurityContext: " + email);
        return ownerRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Owner findEntityById(Long userId) {
        return ownerRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }

    private UserResponseDto mapToResponse(Owner entity) {
        UserResponseDto response = new UserResponseDto();
        modelMapper.map(entity, response);
        return response;
    }

    private void syncImageField(String currentUrl, MultipartFile newFile, String existingUrl, boolean deleteFlag,
                                java.util.function.Consumer<String> setter) {
        if (deleteFlag) {
            if (currentUrl != null) {
                cloudinaryService.deleteFile(currentUrl);
            }
            setter.accept(null);
        } else if (newFile != null && !newFile.isEmpty()) {
            if (currentUrl != null && !currentUrl.equals(existingUrl)) {
                cloudinaryService.deleteFile(currentUrl);
            }
            String uploadedUrl = cloudinaryService.uploadFile(newFile, "userImages");
            setter.accept(uploadedUrl);
        } else if (existingUrl != null && !existingUrl.isEmpty()) {
            setter.accept(existingUrl);
        } else {
            setter.accept(null);
        }
    }

    @Transactional
    public UserResponseDto createUser(UserRequestDto request,
                                      MultipartFile headerLogo,
                                      MultipartFile vendorSignature,
                                      MultipartFile witness1Signature,
                                      MultipartFile witness2Signature) {
        System.out.println("========== CREATE USER DEBUG START ==========");
        System.out.println("Request Name: " + request.getName());
        System.out.println("Request Email: " + request.getEmail());

        // Debug image files
        System.out.println("headerLogo received: " + (headerLogo != null ? headerLogo.getOriginalFilename() : "NULL"));
        System.out.println("headerLogo size: " + (headerLogo != null ? headerLogo.getSize() : 0));
        System.out.println("vendorSignature received: " + (vendorSignature != null ? vendorSignature.getOriginalFilename() : "NULL"));
        System.out.println("witness1Signature received: " + (witness1Signature != null ? witness1Signature.getOriginalFilename() : "NULL"));
        System.out.println("witness2Signature received: " + (witness2Signature != null ? witness2Signature.getOriginalFilename() : "NULL"));

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

        user.setVendorAddress(request.getVendorAddress());
        user.setAuthorizedPersonName(request.getAuthorizedPersonName());
        user.setWitness1Name(request.getWitness1Name());
        user.setWitness1Address(request.getWitness1Address());
        user.setWitness2Name(request.getWitness2Name());
        user.setWitness2Address(request.getWitness2Address());
        user.setBankAccountName(request.getBankAccountName());
        user.setBankAccountNumber(request.getBankAccountNumber());
        user.setBankName(request.getBankName());
        user.setBankIfscCode(request.getBankIfscCode());
        user.setBranchName(request.getBranchName());
        user.setDesignation(request.getDesignation());

        if (headerLogo != null && !headerLogo.isEmpty()) {
            try {
                System.out.println("Uploading header logo to Cloudinary...");
                String uploadedUrl = cloudinaryService.uploadFile(headerLogo, "userHeaderLogos");
                System.out.println("Header logo uploaded URL: " + uploadedUrl);
                user.setHeaderLogoUrl(uploadedUrl);
            } catch (Exception e) {
                System.out.println("ERROR uploading header logo: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("No header logo received - skipping upload");
        }

        if (vendorSignature != null && !vendorSignature.isEmpty()) {
            user.setVendorSignatureUrl(cloudinaryService.uploadFile(vendorSignature, "userVendorSignatures"));
            System.out.println("Vendor signature uploaded");
        }
        if (witness1Signature != null && !witness1Signature.isEmpty()) {
            user.setWitness1SignatureUrl(cloudinaryService.uploadFile(witness1Signature, "userWitnessSignatures"));
            System.out.println("Witness1 signature uploaded");
        }
        if (witness2Signature != null && !witness2Signature.isEmpty()) {
            user.setWitness2SignatureUrl(cloudinaryService.uploadFile(witness2Signature, "userWitnessSignatures"));
            System.out.println("Witness2 signature uploaded");
        }

        Owner savedUser = ownerRepository.save(user);
        System.out.println("User saved with ID: " + savedUser.getId());
        System.out.println("Header Logo URL in saved user: " + savedUser.getHeaderLogoUrl());
        System.out.println("========== CREATE USER DEBUG END ==========");

        return mapToResponse(savedUser);
    }

    public List<UserResponseDto> getAllUsers() {
        return ownerRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public UserResponseDto getUserById(Long userId) {
        return mapToResponse(findEntityById(userId));
    }

    @Transactional
    public UserResponseDto updateUser(Long userId,
                                      UserRequestDto request,
                                      MultipartFile headerLogo,
                                      MultipartFile vendorSignature,
                                      MultipartFile witness1Signature,
                                      MultipartFile witness2Signature) {

        Owner existingUser = findEntityById(userId);

        if (existingUser.getRole() == UserRole.SUPER_ADMIN) {
            throw new RuntimeException("Cannot modify SUPER_ADMIN user");
        }

        if (request.getName() != null && !request.getName().isEmpty()) {
            existingUser.setName(request.getName());
        }
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            existingUser.setEmail(request.getEmail());
        }
        if (request.getMobile() != null && !request.getMobile().isEmpty()) {
            existingUser.setMobile(request.getMobile());
        }
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getPermissions() != null) {
            existingUser.setPermissions(request.getPermissions());
        }
        if (request.getIsActive() != null) {
            existingUser.setActive(request.getIsActive());
        }

        if (request.getVendorAddress() != null) {
            existingUser.setVendorAddress(request.getVendorAddress().isEmpty() ? null : request.getVendorAddress());
        }
        if (request.getAuthorizedPersonName() != null) {
            existingUser.setAuthorizedPersonName(request.getAuthorizedPersonName().isEmpty() ? null : request.getAuthorizedPersonName());
        }
        if (request.getWitness1Name() != null) {
            existingUser.setWitness1Name(request.getWitness1Name().isEmpty() ? null : request.getWitness1Name());
        }
        if (request.getWitness1Address() != null) {
            existingUser.setWitness1Address(request.getWitness1Address().isEmpty() ? null : request.getWitness1Address());
        }
        if (request.getWitness2Name() != null) {
            existingUser.setWitness2Name(request.getWitness2Name().isEmpty() ? null : request.getWitness2Name());
        }
        if (request.getWitness2Address() != null) {
            existingUser.setWitness2Address(request.getWitness2Address().isEmpty() ? null : request.getWitness2Address());
        }
        if (request.getDesignation() != null) {
            existingUser.setDesignation(request.getDesignation().isEmpty() ? null : request.getDesignation());
        }

        if (request.getBankAccountName() != null) {
            existingUser.setBankAccountName(request.getBankAccountName().isEmpty() ? null : request.getBankAccountName());
        }
        if (request.getBankAccountNumber() != null) {
            existingUser.setBankAccountNumber(request.getBankAccountNumber().isEmpty() ? null : request.getBankAccountNumber());
        }
        if (request.getBankName() != null) {
            existingUser.setBankName(request.getBankName().isEmpty() ? null : request.getBankName());
        }
        if (request.getBankIfscCode() != null) {
            existingUser.setBankIfscCode(request.getBankIfscCode().isEmpty() ? null : request.getBankIfscCode());
        }
        if (request.getBranchName() != null) {
            existingUser.setBranchName(request.getBranchName().isEmpty() ? null : request.getBranchName());
        }

        syncImageField(existingUser.getHeaderLogoUrl(), headerLogo,
                request.getExistingHeaderLogo(),
                request.isDeleteHeaderLogo(),
                url -> existingUser.setHeaderLogoUrl(url));

        syncImageField(existingUser.getVendorSignatureUrl(), vendorSignature,
                request.getExistingVendorSignature(),
                request.isDeleteVendorSignature(),
                url -> existingUser.setVendorSignatureUrl(url));

        syncImageField(existingUser.getWitness1SignatureUrl(), witness1Signature,
                request.getExistingWitness1Signature(),
                request.isDeleteWitness1Signature(),
                url -> existingUser.setWitness1SignatureUrl(url));

        syncImageField(existingUser.getWitness2SignatureUrl(), witness2Signature,
                request.getExistingWitness2Signature(),
                request.isDeleteWitness2Signature(),
                url -> existingUser.setWitness2SignatureUrl(url));

        return mapToResponse(ownerRepository.save(existingUser));
    }

    public UserResponseDto updateUserPermissions(Long userId, Set<Permission> permissions) {
        Owner user = findEntityById(userId);
        if (user.getRole() == UserRole.SUPER_ADMIN) {
            throw new RuntimeException("Cannot modify SUPER_ADMIN permissions");
        }
        user.setPermissions(permissions);
        return mapToResponse(ownerRepository.save(user));
    }

    public UserResponseDto updateUserStatus(Long userId, boolean isActive) {
        Owner user = findEntityById(userId);
        user.setActive(isActive);
        return mapToResponse(ownerRepository.save(user));
    }

    public void deleteUser(Long userId) {
        Owner user = findEntityById(userId);
        if (user.getRole() == UserRole.SUPER_ADMIN) {
            throw new RuntimeException("Cannot delete SUPER_ADMIN");
        }

        if (user.getHeaderLogoUrl() != null) {
            cloudinaryService.deleteFile(user.getHeaderLogoUrl());
        }
        if (user.getVendorSignatureUrl() != null) {
            cloudinaryService.deleteFile(user.getVendorSignatureUrl());
        }
        if (user.getWitness1SignatureUrl() != null) {
            cloudinaryService.deleteFile(user.getWitness1SignatureUrl());
        }
        if (user.getWitness2SignatureUrl() != null) {
            cloudinaryService.deleteFile(user.getWitness2SignatureUrl());
        }

        List<SolarRecord> userRecords = solarRecordRepository.findByCreatedByUserEmail(user.getEmail());

        if (!userRecords.isEmpty()) {
            for (SolarRecord record : userRecords) {
                if (record.getSitePhotos() != null && !record.getSitePhotos().isEmpty()) {
                    cloudinaryService.deleteFiles(record.getSitePhotos());
                }
                if (record.getAadharImages() != null && !record.getAadharImages().isEmpty()) {
                    cloudinaryService.deleteFiles(record.getAadharImages());
                }
                if (record.getConsumerSignature() != null && !record.getConsumerSignature().isEmpty()) {
                    cloudinaryService.deleteFiles(record.getConsumerSignature());
                }
                if (record.getNetMeteringStamp() != null && !record.getNetMeteringStamp().isEmpty()) {
                    cloudinaryService.deleteFiles(record.getNetMeteringStamp());
                }
                if (record.getAnnexureTwoStamp() != null && !record.getAnnexureTwoStamp().isEmpty()) {
                    cloudinaryService.deleteFiles(record.getAnnexureTwoStamp());
                }
            }
            solarRecordRepository.deleteAll(userRecords);
        }

        ownerRepository.delete(user);
    }

    public UserResponseDto getCurrentUserProfile() {
        Owner currentUser = getCurrentUser();
        System.out.println("========== GET CURRENT USER PROFILE DEBUG ==========");
        System.out.println("User ID: " + currentUser.getId());
        System.out.println("Header Logo URL from DB: " + currentUser.getHeaderLogoUrl());
        System.out.println("Vendor Signature URL from DB: " + currentUser.getVendorSignatureUrl());
        System.out.println("Witness1 Signature URL from DB: " + currentUser.getWitness1SignatureUrl());
        System.out.println("Witness2 Signature URL from DB: " + currentUser.getWitness2SignatureUrl());

        UserResponseDto response = mapToResponse(currentUser);

        // Explicitly set image URLs (ModelMapper might miss them)
        response.setHeaderLogoUrl(currentUser.getHeaderLogoUrl());
        response.setVendorSignatureUrl(currentUser.getVendorSignatureUrl());
        response.setWitness1SignatureUrl(currentUser.getWitness1SignatureUrl());
        response.setWitness2SignatureUrl(currentUser.getWitness2SignatureUrl());

        System.out.println("Response Header Logo URL: " + response.getHeaderLogoUrl());
        System.out.println("========== GET CURRENT USER PROFILE DEBUG END ==========");

        return response;
    }

    @Transactional
    public UserResponseDto updateCurrentUserProfile(UserRequestDto request,
                                                    MultipartFile headerLogo,
                                                    MultipartFile vendorSignature,
                                                    MultipartFile witness1Signature,
                                                    MultipartFile witness2Signature) {

        System.out.println("[DEBUG] ========== updateCurrentUserProfile START ==========");

        Owner currentUser = getCurrentUser();
        System.out.println("[DEBUG] Current User ID: " + currentUser.getId());
        System.out.println("[DEBUG] Current User Email: " + currentUser.getEmail());
        System.out.println("[DEBUG] Current User Name: " + currentUser.getName());
        System.out.println("[DEBUG] Current User Mobile: " + currentUser.getMobile());
        System.out.println("[DEBUG] Current User Role: " + currentUser.getRole());

        if (request != null) {
            System.out.println("[DEBUG] Request Name: " + request.getName());
            System.out.println("[DEBUG] Request Email: " + request.getEmail());
            System.out.println("[DEBUG] Request Mobile: " + request.getMobile());
            System.out.println("[DEBUG] Request Password provided: " + (request.getPassword() != null && !request.getPassword().isEmpty()));

            if (currentUser.getRole() == UserRole.SUPER_ADMIN) {
                if (request.getName() != null && !request.getName().isEmpty()) {
                    currentUser.setName(request.getName());
                    System.out.println("[DEBUG] Updated Name to: " + request.getName());
                }
                if (request.getEmail() != null && !request.getEmail().isEmpty()) {
                    currentUser.setEmail(request.getEmail());
                    System.out.println("[DEBUG] Updated Email to: " + request.getEmail());
                }
                if (request.getMobile() != null && !request.getMobile().isEmpty()) {
                    currentUser.setMobile(request.getMobile());
                    System.out.println("[DEBUG] Updated Mobile to: " + request.getMobile());
                }
                if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                    String encodedPassword = passwordEncoder.encode(request.getPassword());
                    currentUser.setPassword(encodedPassword);
                    System.out.println("[DEBUG] Updated Password - Hash length: " + encodedPassword.length());
                }
            } else {
                System.out.println("[DEBUG] User is not SUPER_ADMIN, skipping basic info update");
            }

            if (request.getVendorAddress() != null) {
                currentUser.setVendorAddress(request.getVendorAddress().isEmpty() ? null : request.getVendorAddress());
            }
            if (request.getAuthorizedPersonName() != null) {
                currentUser.setAuthorizedPersonName(request.getAuthorizedPersonName().isEmpty() ? null : request.getAuthorizedPersonName());
            }
            if (request.getWitness1Name() != null) {
                currentUser.setWitness1Name(request.getWitness1Name().isEmpty() ? null : request.getWitness1Name());
            }
            if (request.getWitness1Address() != null) {
                currentUser.setWitness1Address(request.getWitness1Address().isEmpty() ? null : request.getWitness1Address());
            }
            if (request.getWitness2Name() != null) {
                currentUser.setWitness2Name(request.getWitness2Name().isEmpty() ? null : request.getWitness2Name());
            }
            if (request.getWitness2Address() != null) {
                currentUser.setWitness2Address(request.getWitness2Address().isEmpty() ? null : request.getWitness2Address());
            }
            if (request.getDesignation() != null) {
                currentUser.setDesignation(request.getDesignation().isEmpty() ? null : request.getDesignation());
            }
            if (request.getBankAccountName() != null) {
                currentUser.setBankAccountName(request.getBankAccountName().isEmpty() ? null : request.getBankAccountName());
            }
            if (request.getBankAccountNumber() != null) {
                currentUser.setBankAccountNumber(request.getBankAccountNumber().isEmpty() ? null : request.getBankAccountNumber());
            }
            if (request.getBankName() != null) {
                currentUser.setBankName(request.getBankName().isEmpty() ? null : request.getBankName());
            }
            if (request.getBankIfscCode() != null) {
                currentUser.setBankIfscCode(request.getBankIfscCode().isEmpty() ? null : request.getBankIfscCode());
            }
            if (request.getBranchName() != null) {
                currentUser.setBranchName(request.getBranchName().isEmpty() ? null : request.getBranchName());
            }
        }

        syncImageField(currentUser.getHeaderLogoUrl(), headerLogo,
                request != null ? request.getExistingHeaderLogo() : null,
                request != null && request.isDeleteHeaderLogo(),
                url -> currentUser.setHeaderLogoUrl(url));

        syncImageField(currentUser.getVendorSignatureUrl(), vendorSignature,
                request != null ? request.getExistingVendorSignature() : null,
                request != null && request.isDeleteVendorSignature(),
                url -> currentUser.setVendorSignatureUrl(url));

        syncImageField(currentUser.getWitness1SignatureUrl(), witness1Signature,
                request != null ? request.getExistingWitness1Signature() : null,
                request != null && request.isDeleteWitness1Signature(),
                url -> currentUser.setWitness1SignatureUrl(url));

        syncImageField(currentUser.getWitness2SignatureUrl(), witness2Signature,
                request != null ? request.getExistingWitness2Signature() : null,
                request != null && request.isDeleteWitness2Signature(),
                url -> currentUser.setWitness2SignatureUrl(url));

        Owner savedUser = ownerRepository.save(currentUser);
        System.out.println("[DEBUG] After Save - User ID: " + savedUser.getId());
        System.out.println("[DEBUG] After Save - Email: " + savedUser.getEmail());
        System.out.println("[DEBUG] After Save - Name: " + savedUser.getName());
        System.out.println("[DEBUG] After Save - Mobile: " + savedUser.getMobile());
        System.out.println("[DEBUG] ========== updateCurrentUserProfile END ==========");

        return mapToResponse(savedUser);
    }
}