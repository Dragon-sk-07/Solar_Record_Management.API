package com.suraj.Customer_Portal_29.service;

import com.suraj.Customer_Portal_29.dto.request.SolarRecordRequestDto;
import com.suraj.Customer_Portal_29.dto.response.SolarRecordResponseDto;
import com.suraj.Customer_Portal_29.entity.SolarRecord;
import com.suraj.Customer_Portal_29.entity.Owner;
import com.suraj.Customer_Portal_29.repository.SolarRecordRepository;
import com.suraj.Customer_Portal_29.repository.OwnerRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.modelmapper.ModelMapper;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SolarRecordService {
    private final SolarRecordRepository repository;
    private final ModelMapper modelMapper;
    private final CloudinaryService cloudinaryService;
    private final OwnerRepository ownerRepository;

    public SolarRecordService(SolarRecordRepository repository, ModelMapper modelMapper, CloudinaryService cloudinaryService, OwnerRepository ownerRepository) {
        this.repository = repository;
        this.modelMapper = modelMapper;
        this.cloudinaryService = cloudinaryService;
        this.ownerRepository = ownerRepository;
    }

    private Owner getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ownerRepository.findByEmailIgnoreCase(email.trim()).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public SolarRecordResponseDto save(SolarRecordRequestDto request) {
        Owner currentUser = getCurrentUser();
        SolarRecord entity = mapToEntity(request);
        entity.setCreatedBy(currentUser);
        entity.setCreatedByUserEmail(currentUser.getEmail());
        return mapToResponse(repository.save(entity));
    }

    public List<SolarRecordResponseDto> findAll() {
        Owner currentUser = getCurrentUser();
        List<SolarRecord> records = repository.findByCreatedById(currentUser.getId());
        return records.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Cacheable(value = "solarRecords", key = "#id")
    public SolarRecordResponseDto findById(String id) {
        return mapToResponse(findEntityById(id));
    }

    @CacheEvict(value = "solarRecords", key = "#id")
    public SolarRecordResponseDto update(String id, SolarRecordRequestDto request) {
        Owner currentUser = getCurrentUser();
        SolarRecord existing = findEntityById(id);
        if (!existing.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("No permission to update this record");
        }
        updateEntity(existing, request);
        return mapToResponse(repository.save(existing));
    }

    public void delete(String id) {
        Owner currentUser = getCurrentUser();
        SolarRecord entity = findEntityById(id);
        if (!entity.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("No permission to delete this record");
        }
        deleteCloudinaryFiles(entity);
        repository.delete(entity);
    }

    private void deleteCloudinaryFiles(SolarRecord entity) {
        List<List<String>> fileLists = Arrays.asList(
                entity.getSitePhotos(),
                entity.getAadharImages(),
                entity.getConsumerSignature(),
                entity.getNetMeteringStamp(),
                entity.getAnnexureTwoStamp()
        );
        fileLists.stream().filter(Objects::nonNull).forEach(cloudinaryService::deleteFiles);
    }

    private SolarRecord findEntityById(String id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Solar record not found with id: " + id));
    }

    private SolarRecord mapToEntity(SolarRecordRequestDto req) {
        SolarRecord entity = new SolarRecord();
        if (req.getName() != null) entity.setName(req.getName());
        if (req.getConsumerNumber() != null) entity.setConsumerNumber(req.getConsumerNumber());
        if (req.getMobileNumber() != null) entity.setMobileNumber(req.getMobileNumber());
        if (req.getEmail() != null) entity.setEmail(req.getEmail());
        if (req.getAadharNumber() != null) entity.setAadharNumber(req.getAadharNumber());
        if (req.getSiteAddress() != null) entity.setSiteAddress(req.getSiteAddress());
        if (req.getPlace() != null) entity.setPlace(req.getPlace());
        if (req.getApplicationNumber() != null) entity.setApplicationNumber(req.getApplicationNumber());
        if (req.getApplicationDate() != null) entity.setApplicationDate(req.getApplicationDate());
        if (req.getSanctionNumber() != null) entity.setSanctionNumber(req.getSanctionNumber());
        if (req.getBillingUnit() != null) entity.setBillingUnit(req.getBillingUnit());
        if (req.getConnectionType() != null) entity.setConnectionType(req.getConnectionType());
        if (req.getSanctionedCapacity() != null) entity.setSanctionedCapacity(req.getSanctionedCapacity());
        if (req.getInstalledCapacity() != null) entity.setInstalledCapacity(req.getInstalledCapacity());
        if (req.getInstallationDate() != null) entity.setInstallationDate(req.getInstallationDate());
        if (req.getModuleMake() != null) entity.setModuleMake(req.getModuleMake());
        if (req.getWattagePerModule() != null) entity.setWattagePerModule(req.getWattagePerModule());
        if (req.getNumberOfModules() != null) entity.setNumberOfModules(req.getNumberOfModules());
        if (req.getTotalCapacityKWP() != null) entity.setTotalCapacityKWP(req.getTotalCapacityKWP());
        if (req.getModuleSerialNumbers() != null) entity.setModuleSerialNumbers(req.getModuleSerialNumbers());
        if (req.getNumberOfStrings() != null) entity.setNumberOfStrings(req.getNumberOfStrings());
        if (req.getInverterMake() != null) entity.setInverterMake(req.getInverterMake());
        if (req.getInverterModelNumber() != null) entity.setInverterModelNumber(req.getInverterModelNumber());
        if (req.getInverterCapacity() != null) entity.setInverterCapacity(req.getInverterCapacity());
        if (req.getCellManufacturerName() != null) entity.setCellManufacturerName(req.getCellManufacturerName());
        if (req.getCellGSTInvoiceNo() != null) entity.setCellGSTInvoiceNo(req.getCellGSTInvoiceNo());
        if (req.getMeterMake() != null) entity.setMeterMake(req.getMeterMake());
        if (req.getMeterNumber() != null) entity.setMeterNumber(req.getMeterNumber());
        if (req.getNetMeterNumber() != null) entity.setNetMeterNumber(req.getNetMeterNumber());
        if (req.getInvoiceNumber() != null) entity.setInvoiceNumber(req.getInvoiceNumber());
        if (req.getTotalAmountIncludingGST() != null) entity.setTotalAmountIncludingGST(req.getTotalAmountIncludingGST());
        if (req.getYearOfManufacturing() != null) entity.setYearOfManufacturing(req.getYearOfManufacturing());
        return entity;
    }

    private void updateEntity(SolarRecord entity, SolarRecordRequestDto req) {
        if (req.getName() != null) entity.setName(req.getName());
        if (req.getConsumerNumber() != null) entity.setConsumerNumber(req.getConsumerNumber());
        if (req.getMobileNumber() != null) entity.setMobileNumber(req.getMobileNumber());
        if (req.getEmail() != null) entity.setEmail(req.getEmail());
        if (req.getAadharNumber() != null) entity.setAadharNumber(req.getAadharNumber());
        if (req.getSiteAddress() != null) entity.setSiteAddress(req.getSiteAddress());
        if (req.getPlace() != null) entity.setPlace(req.getPlace());
        if (req.getApplicationNumber() != null) entity.setApplicationNumber(req.getApplicationNumber());
        if (req.getApplicationDate() != null) entity.setApplicationDate(req.getApplicationDate());
        if (req.getSanctionNumber() != null) entity.setSanctionNumber(req.getSanctionNumber());
        if (req.getBillingUnit() != null) entity.setBillingUnit(req.getBillingUnit());
        if (req.getConnectionType() != null) entity.setConnectionType(req.getConnectionType());
        if (req.getSanctionedCapacity() != null) entity.setSanctionedCapacity(req.getSanctionedCapacity());
        if (req.getInstalledCapacity() != null) entity.setInstalledCapacity(req.getInstalledCapacity());
        if (req.getInstallationDate() != null) entity.setInstallationDate(req.getInstallationDate());
        if (req.getModuleMake() != null) entity.setModuleMake(req.getModuleMake());
        if (req.getWattagePerModule() != null) entity.setWattagePerModule(req.getWattagePerModule());
        if (req.getNumberOfModules() != null) entity.setNumberOfModules(req.getNumberOfModules());
        if (req.getTotalCapacityKWP() != null) entity.setTotalCapacityKWP(req.getTotalCapacityKWP());
        if (req.getModuleSerialNumbers() != null) entity.setModuleSerialNumbers(req.getModuleSerialNumbers());
        if (req.getNumberOfStrings() != null) entity.setNumberOfStrings(req.getNumberOfStrings());
        if (req.getInverterMake() != null) entity.setInverterMake(req.getInverterMake());
        if (req.getInverterModelNumber() != null) entity.setInverterModelNumber(req.getInverterModelNumber());
        if (req.getInverterCapacity() != null) entity.setInverterCapacity(req.getInverterCapacity());
        if (req.getCellManufacturerName() != null) entity.setCellManufacturerName(req.getCellManufacturerName());
        if (req.getCellGSTInvoiceNo() != null) entity.setCellGSTInvoiceNo(req.getCellGSTInvoiceNo());
        if (req.getMeterMake() != null) entity.setMeterMake(req.getMeterMake());
        if (req.getMeterNumber() != null) entity.setMeterNumber(req.getMeterNumber());
        if (req.getNetMeterNumber() != null) entity.setNetMeterNumber(req.getNetMeterNumber());
        if (req.getInvoiceNumber() != null) entity.setInvoiceNumber(req.getInvoiceNumber());
        if (req.getTotalAmountIncludingGST() != null) entity.setTotalAmountIncludingGST(req.getTotalAmountIncludingGST());
        if (req.getYearOfManufacturing() != null) entity.setYearOfManufacturing(req.getYearOfManufacturing());

        entity.setConsumerSignature(syncImages(entity.getConsumerSignature(), req.getConsumerSignature(), req.getExistingConsumerSignature(), req.getDeleteConsumerSignature(), "consumerSignatures"));
        entity.setAadharImages(syncImages(entity.getAadharImages(), req.getAadharImages(), req.getExistingAadharImages(), req.getDeleteAadharImages(), "aadharImages"));
        entity.setSitePhotos(syncImages(entity.getSitePhotos(), req.getSitePhotos(), req.getExistingSitePhotos(), req.getDeleteSitePhotos(), "sitePhotos"));
        entity.setNetMeteringStamp(syncImages(entity.getNetMeteringStamp(), req.getNetMeteringStamp(), req.getExistingNetMeteringStamp(), req.getDeleteNetMeteringStamp(), "netMeteringStamps"));
        entity.setAnnexureTwoStamp(syncImages(entity.getAnnexureTwoStamp(), req.getAnnexureTwoStamp(), req.getExistingAnnexureTwoStamp(), req.getDeleteAnnexureTwoStamp(), "annexureTwoStamps"));
    }

    private SolarRecordResponseDto mapToResponse(SolarRecord entity) {
        SolarRecordResponseDto response = new SolarRecordResponseDto();
        modelMapper.map(entity, response);
        return response;
    }

    private List<String> uploadImages(List<MultipartFile> files, String folder) {
        if (files == null || files.isEmpty()) return Collections.emptyList();
        return files.stream().filter(Objects::nonNull).map(file -> cloudinaryService.uploadFile(file, folder)).collect(Collectors.toList());
    }

    private List<String> syncImages(
            List<String> existingImages,
            List<MultipartFile> newFiles,
            List<String> existingUrlsFromRequest,
            Boolean deleteFlag,
            String folder
    ) {
        boolean hasNew = newFiles != null && newFiles.stream().anyMatch(Objects::nonNull);
        boolean hasExisting = existingUrlsFromRequest != null && !existingUrlsFromRequest.isEmpty();
        boolean explicitDelete = Boolean.TRUE.equals(deleteFlag);

        // Nothing sent → keep DB values untouched (SAFE default)
        if (!hasNew && !hasExisting && !explicitDelete) {
            return existingImages == null ? new ArrayList<>() : new ArrayList<>(existingImages);
        }

        // Explicit delete with no replacements
        if (explicitDelete && !hasNew && !hasExisting) {
            if (existingImages != null && !existingImages.isEmpty()) {
                cloudinaryService.deleteFiles(existingImages);
            }
            return new ArrayList<>();
        }

        // Normal merge: keep requested URLs + upload new files, delete only removed
        List<String> finalUrls = new ArrayList<>();
        if (hasExisting) finalUrls.addAll(existingUrlsFromRequest);
        if (hasNew) finalUrls.addAll(uploadImages(newFiles, folder));

        if (existingImages != null && !existingImages.isEmpty()) {
            List<String> urlsToDelete = existingImages.stream()
                    .filter(url -> !finalUrls.contains(url))
                    .collect(Collectors.toList());
            if (!urlsToDelete.isEmpty()) cloudinaryService.deleteFiles(urlsToDelete);
        }
        return finalUrls;
    }
}