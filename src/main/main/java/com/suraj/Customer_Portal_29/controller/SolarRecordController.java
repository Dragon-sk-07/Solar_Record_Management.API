package com.suraj.Customer_Portal_29.controller;

import com.suraj.Customer_Portal_29.dto.request.SolarRecordRequestDto;
import com.suraj.Customer_Portal_29.dto.response.ApiResponseDto;
import com.suraj.Customer_Portal_29.dto.response.SolarRecordResponseDto;
import com.suraj.Customer_Portal_29.entity.Permission;
import com.suraj.Customer_Portal_29.entity.Owner;
import com.suraj.Customer_Portal_29.entity.UserRole;
import com.suraj.Customer_Portal_29.repository.OwnerRepository;
import com.suraj.Customer_Portal_29.service.SolarRecordService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/solar-records")
public class SolarRecordController {

    private final SolarRecordService service;
    private final OwnerRepository ownerRepository;

    public SolarRecordController(SolarRecordService service, OwnerRepository ownerRepository) {
        this.service = service;
        this.ownerRepository = ownerRepository;
    }

    private void checkPermission(Permission requiredPermission) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Owner currentUser = ownerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (currentUser.getRole() == UserRole.SUPER_ADMIN) {
            return;
        }

        if (!currentUser.getPermissions().contains(requiredPermission)) {
            throw new RuntimeException("Access denied. " + requiredPermission + " permission required.");
        }
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponseDto<SolarRecordResponseDto>> save(
            @Valid @ModelAttribute SolarRecordRequestDto request
    ){
        checkPermission(Permission.CREATE_RECORD);
        SolarRecordResponseDto data = service.save(request);
        return ResponseEntity.status(201).body(
                new ApiResponseDto<>("Solar record created successfully", data)
        );
    }

    @GetMapping
    public ApiResponseDto<List<SolarRecordResponseDto>> getAll() {
        checkPermission(Permission.VIEW_RECORDS);
        List<SolarRecordResponseDto> data = service.findAll();
        return new ApiResponseDto<>("Solar records fetched successfully", data);
    }

    @GetMapping("/{id}")
    public ApiResponseDto<SolarRecordResponseDto> getById(
            @PathVariable String id
    ) {
        checkPermission(Permission.VIEW_RECORDS);
        SolarRecordResponseDto data = service.findById(id);
        return new ApiResponseDto<>("Solar record fetched successfully", data);
    }

    @PutMapping(value="/{id}", consumes="multipart/form-data")
    public ApiResponseDto<SolarRecordResponseDto> update(
            @PathVariable String id,
            @Valid @ModelAttribute SolarRecordRequestDto request
    ) {
        checkPermission(Permission.EDIT_RECORD);
        SolarRecordResponseDto data = service.update(id, request);
        return new ApiResponseDto<>("Solar record updated successfully.", data);
    }

    @DeleteMapping("/{id}")
    public ApiResponseDto<String> delete(
            @PathVariable String id
    ) {
        checkPermission(Permission.DELETE_RECORD);
        service.delete(id);
        return new ApiResponseDto<>("Solar record deleted successfully", null);
    }
}