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

    private void updateImageField(String currentUrl, MultipartFile newFile, String existingUrl, boolean deleteFlag,
                                  java.util.function.Consumer<String> setter, java.util.function.Consumer<String> deleter) {
        if (deleteFlag) {
            if (currentUrl != null) {
                cloudinaryService.deleteFile(currentUrl);
            }
            setter.accept(null);
        } else if (newFile != null && !newFile.isEmpty()) {
            if (currentUrl != null) {
                cloudinaryService.deleteFile(currentUrl);
            }
            setter.accept(cloudinaryService.uploadFile(newFile, "userImages"));
        } else if (existingUrl != null && !existingUrl.isEmpty()) {
            setter.accept(existingUrl);
        }
    }

    @Transactional
    public UserResponseDto createUser(UserRequestDto request,
                                      MultipartFile headerLogo,
                                      MultipartFile vendorSignature,
                                      MultipartFile witness1Signature,
                                      MultipartFile witness2Signature) {
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

        // Set permissions
        if (request.getPermissions() == null || request.getPermissions().isEmpty()) {
            user.setPermissions(Set.of(Permission.VIEW_RECORDS));
        } else {
            user.setPermissions(request.getPermissions());
        }

        // Optional fields
        user.setVendorAddress(request.getVendorAddress());
        user.setAuthorizedPersonName(request.getAuthorizedPersonName());
        user.setWitness1Name(request.getWitness1Name());
        user.setWitness1Address(request.getWitness1Address());
        user.setWitness2Name(request.getWitness2Name());
        user.setWitness2Address(request.getWitness2Address());
        user.setVendorMobile(request.getVendorMobile());
        user.setVendorEmail(request.getVendorEmail());
        user.setBankAccountName(request.getBankAccountName());
        user.setBankAccountNumber(request.getBankAccountNumber());
        user.setBankName(request.getBankName());
        user.setBankIfscCode(request.getBankIfscCode());
        user.setBranchName(request.getBranchName());
        user.setDesignation(request.getDesignation());

        // Upload images if provided
        if (headerLogo != null && !headerLogo.isEmpty()) {
            user.setHeaderLogoUrl(cloudinaryService.uploadFile(headerLogo, "userHeaderLogos"));
        }
        if (vendorSignature != null && !vendorSignature.isEmpty()) {
            user.setVendorSignatureUrl(cloudinaryService.uploadFile(vendorSignature, "userVendorSignatures"));
        }
        if (witness1Signature != null && !witness1Signature.isEmpty()) {
            user.setWitness1SignatureUrl(cloudinaryService.uploadFile(witness1Signature, "userWitnessSignatures"));
        }
        if (witness2Signature != null && !witness2Signature.isEmpty()) {
            user.setWitness2SignatureUrl(cloudinaryService.uploadFile(witness2Signature, "userWitnessSignatures"));
        }

        return mapToResponse(ownerRepository.save(user));
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

        // SUPER_ADMIN cannot be modified by this endpoint
        if (existingUser.getRole() == UserRole.SUPER_ADMIN) {
            throw new RuntimeException("Cannot modify SUPER_ADMIN user");
        }

        // Update basic info (name, email, mobile, password, permissions)
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

        // Update optional fields
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
        if (request.getVendorMobile() != null) {
            existingUser.setVendorMobile(request.getVendorMobile().isEmpty() ? null : request.getVendorMobile());
        }
        if (request.getVendorEmail() != null) {
            existingUser.setVendorEmail(request.getVendorEmail().isEmpty() ? null : request.getVendorEmail());
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

        // Handle Header Logo update/delete
        updateImageField(existingUser.getHeaderLogoUrl(), headerLogo,
                request.getExistingHeaderLogo(),
                request.isDeleteHeaderLogo(),
                url -> existingUser.setHeaderLogoUrl(url),
                url -> {});

        // Handle signatures
        updateImageField(existingUser.getVendorSignatureUrl(), vendorSignature,
                request.getExistingVendorSignature(),
                request.isDeleteVendorSignature(),
                url -> existingUser.setVendorSignatureUrl(url),
                url -> {});

        updateImageField(existingUser.getWitness1SignatureUrl(), witness1Signature,
                request.getExistingWitness1Signature(),
                request.isDeleteWitness1Signature(),
                url -> existingUser.setWitness1SignatureUrl(url),
                url -> {});

        updateImageField(existingUser.getWitness2SignatureUrl(), witness2Signature,
                request.getExistingWitness2Signature(),
                request.isDeleteWitness2Signature(),
                url -> existingUser.setWitness2SignatureUrl(url),
                url -> {});

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
        return mapToResponse(currentUser);
    }

    @Transactional
    public UserResponseDto updateCurrentUserProfile(UserRequestDto request,
                                                    MultipartFile headerLogo,
                                                    MultipartFile vendorSignature,
                                                    MultipartFile witness1Signature,
                                                    MultipartFile witness2Signature) {

        Owner currentUser = getCurrentUser();

        if (request != null) {
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
            if (request.getVendorMobile() != null) {
                currentUser.setVendorMobile(request.getVendorMobile().isEmpty() ? null : request.getVendorMobile());
            }
            if (request.getVendorEmail() != null) {
                currentUser.setVendorEmail(request.getVendorEmail().isEmpty() ? null : request.getVendorEmail());
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

        updateImageField(currentUser.getHeaderLogoUrl(), headerLogo,
                request != null ? request.getExistingHeaderLogo() : null,
                request != null && request.isDeleteHeaderLogo(),
                url -> currentUser.setHeaderLogoUrl(url),
                url -> {});

        updateImageField(currentUser.getVendorSignatureUrl(), vendorSignature,
                request != null ? request.getExistingVendorSignature() : null,
                request != null && request.isDeleteVendorSignature(),
                url -> currentUser.setVendorSignatureUrl(url),
                url -> {});

        updateImageField(currentUser.getWitness1SignatureUrl(), witness1Signature,
                request != null ? request.getExistingWitness1Signature() : null,
                request != null && request.isDeleteWitness1Signature(),
                url -> currentUser.setWitness1SignatureUrl(url),
                url -> {});

        updateImageField(currentUser.getWitness2SignatureUrl(), witness2Signature,
                request != null ? request.getExistingWitness2Signature() : null,
                request != null && request.isDeleteWitness2Signature(),
                url -> currentUser.setWitness2SignatureUrl(url),
                url -> {});

        return mapToResponse(ownerRepository.save(currentUser));
    }
}