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

    // CREATE USER
    public UserResponseDto createUser(UserRequestDto request) {
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

        return mapToResponse(ownerRepository.save(user));
    }

    // GET ALL USERS
    public List<UserResponseDto> getAllUsers() {
        return ownerRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // GET USER BY ID
    public UserResponseDto getUserById(Long userId) {
        return mapToResponse(findEntityById(userId));
    }

    // UPDATE USER (for SUPER_ADMIN)
    @Transactional
    public UserResponseDto updateUser(Long userId,
                                      UserRequestDto request,
                                      MultipartFile headerLogo,
                                      MultipartFile vendorSignature,
                                      MultipartFile witness1Signature,
                                      MultipartFile witness2Signature) {

        System.out.println("=== UserManagementService.updateUser START ===");
        Owner existingUser = findEntityById(userId);

        // Map all non-null fields from request to entity
        modelMapper.map(request, existingUser);

        // Handle file uploads
        if (headerLogo != null && !headerLogo.isEmpty()) {
            if (existingUser.getHeaderLogoUrl() != null) {
                cloudinaryService.deleteFile(existingUser.getHeaderLogoUrl());
            }
            existingUser.setHeaderLogoUrl(cloudinaryService.uploadFile(headerLogo, "userHeaderLogos"));
        }

        if (vendorSignature != null && !vendorSignature.isEmpty()) {
            if (existingUser.getVendorSignatureUrl() != null) {
                cloudinaryService.deleteFile(existingUser.getVendorSignatureUrl());
            }
            existingUser.setVendorSignatureUrl(cloudinaryService.uploadFile(vendorSignature, "userVendorSignatures"));
        }

        if (witness1Signature != null && !witness1Signature.isEmpty()) {
            if (existingUser.getWitness1SignatureUrl() != null) {
                cloudinaryService.deleteFile(existingUser.getWitness1SignatureUrl());
            }
            existingUser.setWitness1SignatureUrl(cloudinaryService.uploadFile(witness1Signature, "userWitnessSignatures"));
        }

        if (witness2Signature != null && !witness2Signature.isEmpty()) {
            if (existingUser.getWitness2SignatureUrl() != null) {
                cloudinaryService.deleteFile(existingUser.getWitness2SignatureUrl());
            }
            existingUser.setWitness2SignatureUrl(cloudinaryService.uploadFile(witness2Signature, "userWitnessSignatures"));
        }

        Owner savedUser = ownerRepository.save(existingUser);
        System.out.println("User updated successfully - ID: " + savedUser.getId());

        return mapToResponse(savedUser);
    }

    // UPDATE PERMISSIONS
    public UserResponseDto updateUserPermissions(Long userId, Set<Permission> permissions) {
        Owner user = findEntityById(userId);
        if (user.getRole() == UserRole.SUPER_ADMIN) {
            throw new RuntimeException("Cannot modify SUPER_ADMIN permissions");
        }
        user.setPermissions(permissions);
        return mapToResponse(ownerRepository.save(user));
    }

    // UPDATE STATUS
    public UserResponseDto updateUserStatus(Long userId, boolean isActive) {
        Owner user = findEntityById(userId);
        user.setActive(isActive);
        return mapToResponse(ownerRepository.save(user));
    }

    // DELETE USER
    public void deleteUser(Long userId) {
        Owner user = findEntityById(userId);
        if (user.getRole() == UserRole.SUPER_ADMIN) {
            throw new RuntimeException("Cannot delete SUPER_ADMIN");
        }

        // Delete Cloudinary images
        if (user.getHeaderLogoUrl() != null) cloudinaryService.deleteFile(user.getHeaderLogoUrl());
        if (user.getVendorSignatureUrl() != null) cloudinaryService.deleteFile(user.getVendorSignatureUrl());
        if (user.getWitness1SignatureUrl() != null) cloudinaryService.deleteFile(user.getWitness1SignatureUrl());
        if (user.getWitness2SignatureUrl() != null) cloudinaryService.deleteFile(user.getWitness2SignatureUrl());

        // Delete associated solar records
        List<SolarRecord> userRecords = solarRecordRepository.findByCreatedByUserEmail(user.getEmail());
        if (!userRecords.isEmpty()) {
            solarRecordRepository.deleteAll(userRecords);
        }

        ownerRepository.delete(user);
    }

    // GET CURRENT USER PROFILE
    public UserResponseDto getCurrentUserProfile() {
        Owner currentUser = getCurrentUser();
        System.out.println("=== GET CURRENT USER PROFILE ===");
        System.out.println("User ID: " + currentUser.getId());
        System.out.println("Vendor Address: " + currentUser.getVendorAddress());
        System.out.println("Witness1 Name: " + currentUser.getWitness1Name());
        return mapToResponse(currentUser);
    }

    // UPDATE CURRENT USER PROFILE
    @Transactional
    public UserResponseDto updateCurrentUserProfile(UserRequestDto request,
                                                    MultipartFile headerLogo,
                                                    MultipartFile vendorSignature,
                                                    MultipartFile witness1Signature,
                                                    MultipartFile witness2Signature) {

        System.out.println("=== UPDATE CURRENT USER PROFILE ===");
        System.out.println("Received vendorAddress: " + request.getVendorAddress());
        System.out.println("Received witness1Name: " + request.getWitness1Name());

        Owner currentUser = getCurrentUser();

        // Map all non-null fields from request to entity
        modelMapper.map(request, currentUser);

        // Handle file uploads
        if (headerLogo != null && !headerLogo.isEmpty()) {
            if (currentUser.getHeaderLogoUrl() != null) {
                cloudinaryService.deleteFile(currentUser.getHeaderLogoUrl());
            }
            currentUser.setHeaderLogoUrl(cloudinaryService.uploadFile(headerLogo, "userHeaderLogos"));
        }

        if (vendorSignature != null && !vendorSignature.isEmpty()) {
            if (currentUser.getVendorSignatureUrl() != null) {
                cloudinaryService.deleteFile(currentUser.getVendorSignatureUrl());
            }
            currentUser.setVendorSignatureUrl(cloudinaryService.uploadFile(vendorSignature, "userVendorSignatures"));
        }

        if (witness1Signature != null && !witness1Signature.isEmpty()) {
            if (currentUser.getWitness1SignatureUrl() != null) {
                cloudinaryService.deleteFile(currentUser.getWitness1SignatureUrl());
            }
            currentUser.setWitness1SignatureUrl(cloudinaryService.uploadFile(witness1Signature, "userWitnessSignatures"));
        }

        if (witness2Signature != null && !witness2Signature.isEmpty()) {
            if (currentUser.getWitness2SignatureUrl() != null) {
                cloudinaryService.deleteFile(currentUser.getWitness2SignatureUrl());
            }
            currentUser.setWitness2SignatureUrl(cloudinaryService.uploadFile(witness2Signature, "userWitnessSignatures"));
        }

        Owner savedUser = ownerRepository.save(currentUser);
        System.out.println("After update - Vendor Address: " + savedUser.getVendorAddress());
        System.out.println("After update - Witness1 Name: " + savedUser.getWitness1Name());

        return mapToResponse(savedUser);
    }
}