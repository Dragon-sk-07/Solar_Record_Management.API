package com.suraj.Customer_Portal_29.service;

import com.suraj.Customer_Portal_29.dto.request.SolarRecordRequestDto;
import com.suraj.Customer_Portal_29.dto.response.SolarRecordResponseDto;
import com.suraj.Customer_Portal_29.entity.SolarRecord;
import com.suraj.Customer_Portal_29.entity.Owner;
import com.suraj.Customer_Portal_29.repository.SolarRecordRepository;
import com.suraj.Customer_Portal_29.repository.OwnerRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.suraj.Customer_Portal_29.config.CurrentUserContext;
import org.springframework.web.multipart.MultipartFile;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
public class SolarRecordService {

    private final SolarRecordRepository repository;
    private final ModelMapper modelMapper;
    private final CloudinaryService cloudinaryService;
    private final OwnerRepository ownerRepository;
    private final PdfCacheService pdfCacheService;
    private final Executor cloudinaryPool;

    public SolarRecordService(SolarRecordRepository repository,
                              ModelMapper modelMapper,
                              CloudinaryService cloudinaryService,
                              OwnerRepository ownerRepository,
                              PdfCacheService pdfCacheService,
                              @Qualifier("cloudinaryPool") Executor cloudinaryPool) {
        this.repository = repository;
        this.modelMapper = modelMapper;
        this.cloudinaryService = cloudinaryService;
        this.ownerRepository = ownerRepository;
        this.pdfCacheService = pdfCacheService;
        this.cloudinaryPool = cloudinaryPool;
    }

    private Owner getCurrentUser() {
        Owner cached = CurrentUserContext.get();
        if (cached != null) return cached;
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Owner user = ownerRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new RuntimeException("User not found"));
        CurrentUserContext.set(user);
        return user;
    }

    @CacheEvict(value = { "solarRecords", "solarRecordLists" }, allEntries = true)
    public SolarRecordResponseDto save(SolarRecordRequestDto request) {
        Owner currentUser = getCurrentUser();
        SolarRecord entity = mapToEntity(request);
        entity.setCreatedBy(currentUser);
        entity.setCreatedByUserEmail(currentUser.getEmail());
        return mapToResponse(repository.save(entity));
    }


    @Cacheable(
            value = "solarRecordLists",
            key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()"
    )
    public List<SolarRecordResponseDto> findAll() {
        Owner currentUser = getCurrentUser();
        List<SolarRecord> records = repository.findByCreatedById(currentUser.getId());
        return records.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Cacheable(value = "solarRecords", key = "#id")
    public SolarRecordResponseDto findById(String id) {
        return mapToResponse(findEntityById(id));
    }

    @CacheEvict(value = { "solarRecords", "solarRecordLists" }, allEntries = true)
    public SolarRecordResponseDto update(String id, SolarRecordRequestDto request) {
        Owner currentUser = getCurrentUser();
        SolarRecord existing = findEntityById(id);
        if (!existing.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("No permission to update this record");
        }
        updateEntity(existing, request);
        SolarRecordResponseDto saved = mapToResponse(repository.save(existing));
        pdfCacheService.evictRecord(id);
        return saved;
    }

    @CacheEvict(value = { "solarRecords", "solarRecordLists" }, allEntries = true)
    public void delete(String id) {
        Owner currentUser = getCurrentUser();
        SolarRecord entity = findEntityById(id);
        if (!entity.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("No permission to delete this record");
        }
        deleteCloudinaryFiles(entity);
        repository.delete(entity);
        pdfCacheService.evictRecord(id);
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
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solar record not found with id: " + id));
    }

    private SolarRecord mapToEntity(SolarRecordRequestDto req) {
        SolarRecord entity = new SolarRecord();
        entity.setName(req.getName());
        entity.setConsumerNumber(req.getConsumerNumber());
        entity.setMobileNumber(req.getMobileNumber());
        entity.setEmail(req.getEmail());
        entity.setAadharNumber(req.getAadharNumber());
        entity.setSiteAddress(req.getSiteAddress());
        entity.setPlace(req.getPlace());
        entity.setApplicationNumber(req.getApplicationNumber());
        entity.setApplicationDate(req.getApplicationDate());
        entity.setSanctionNumber(req.getSanctionNumber());
        entity.setBillingUnit(req.getBillingUnit());
        entity.setConnectionType(req.getConnectionType());
        entity.setSanctionedCapacity(req.getSanctionedCapacity());
        entity.setInstalledCapacity(req.getInstalledCapacity());
        entity.setInstallationDate(req.getInstallationDate());
        entity.setModuleMake(req.getModuleMake());
        entity.setWattagePerModule(req.getWattagePerModule());
        entity.setNumberOfModules(req.getNumberOfModules());
        entity.setTotalCapacityKWP(req.getTotalCapacityKWP());
        entity.setModuleSerialNumbers(req.getModuleSerialNumbers());
        entity.setNumberOfStrings(req.getNumberOfStrings());
        entity.setInverterMake(req.getInverterMake());
        entity.setInverterModelNumber(req.getInverterModelNumber());
        entity.setInverterCapacity(req.getInverterCapacity());
        entity.setCellManufacturerName(req.getCellManufacturerName());
        entity.setCellGSTInvoiceNo(req.getCellGSTInvoiceNo());
        entity.setMeterMake(req.getMeterMake());
        entity.setMeterNumber(req.getMeterNumber());
        entity.setNetMeterNumber(req.getNetMeterNumber());
        entity.setInvoiceNumber(req.getInvoiceNumber());
        entity.setTotalAmountIncludingGST(req.getTotalAmountIncludingGST());
        entity.setYearOfManufacturing(req.getYearOfManufacturing());
        entity.setConsumerSignature(uploadImages(req.getConsumerSignature(), "consumerSignatures"));
        entity.setAadharImages(uploadImages(req.getAadharImages(), "aadharImages"));
        entity.setSitePhotos(uploadImages(req.getSitePhotos(), "sitePhotos"));
        entity.setNetMeteringStamp(uploadImages(req.getNetMeteringStamp(), "netMeteringStamps"));
        entity.setAnnexureTwoStamp(uploadImages(req.getAnnexureTwoStamp(), "annexureTwoStamps"));
        return entity;
    }

    private void updateEntity(SolarRecord entity, SolarRecordRequestDto req) {

        if (differs(entity.getName(), req.getName())) entity.setName(req.getName());
        if (differs(entity.getConsumerNumber(), req.getConsumerNumber())) entity.setConsumerNumber(req.getConsumerNumber());
        if (differs(entity.getMobileNumber(), req.getMobileNumber())) entity.setMobileNumber(req.getMobileNumber());
        if (differs(entity.getEmail(), req.getEmail())) entity.setEmail(req.getEmail());
        if (differs(entity.getAadharNumber(), req.getAadharNumber())) entity.setAadharNumber(req.getAadharNumber());
        if (differs(entity.getSiteAddress(), req.getSiteAddress())) entity.setSiteAddress(req.getSiteAddress());
        if (differs(entity.getPlace(), req.getPlace())) entity.setPlace(req.getPlace());
        if (differs(entity.getApplicationNumber(), req.getApplicationNumber())) entity.setApplicationNumber(req.getApplicationNumber());
        if (differs(entity.getApplicationDate(), req.getApplicationDate())) entity.setApplicationDate(req.getApplicationDate());
        if (differs(entity.getSanctionNumber(), req.getSanctionNumber())) entity.setSanctionNumber(req.getSanctionNumber());
        if (differs(entity.getBillingUnit(), req.getBillingUnit())) entity.setBillingUnit(req.getBillingUnit());
        if (differs(entity.getConnectionType(), req.getConnectionType())) entity.setConnectionType(req.getConnectionType());
        if (differs(entity.getSanctionedCapacity(), req.getSanctionedCapacity())) entity.setSanctionedCapacity(req.getSanctionedCapacity());
        if (differs(entity.getInstalledCapacity(), req.getInstalledCapacity())) entity.setInstalledCapacity(req.getInstalledCapacity());
        if (differs(entity.getInstallationDate(), req.getInstallationDate())) entity.setInstallationDate(req.getInstallationDate());
        if (differs(entity.getModuleMake(), req.getModuleMake())) entity.setModuleMake(req.getModuleMake());
        if (differs(entity.getWattagePerModule(), req.getWattagePerModule())) entity.setWattagePerModule(req.getWattagePerModule());
        if (differs(entity.getNumberOfModules(), req.getNumberOfModules())) entity.setNumberOfModules(req.getNumberOfModules());
        if (differs(entity.getTotalCapacityKWP(), req.getTotalCapacityKWP())) entity.setTotalCapacityKWP(req.getTotalCapacityKWP());
        if (differs(entity.getModuleSerialNumbers(), req.getModuleSerialNumbers())) entity.setModuleSerialNumbers(req.getModuleSerialNumbers());
        if (differs(entity.getNumberOfStrings(), req.getNumberOfStrings())) entity.setNumberOfStrings(req.getNumberOfStrings());
        if (differs(entity.getInverterMake(), req.getInverterMake())) entity.setInverterMake(req.getInverterMake());
        if (differs(entity.getInverterModelNumber(), req.getInverterModelNumber())) entity.setInverterModelNumber(req.getInverterModelNumber());
        if (differs(entity.getInverterCapacity(), req.getInverterCapacity())) entity.setInverterCapacity(req.getInverterCapacity());
        if (differs(entity.getCellManufacturerName(), req.getCellManufacturerName())) entity.setCellManufacturerName(req.getCellManufacturerName());
        if (differs(entity.getCellGSTInvoiceNo(), req.getCellGSTInvoiceNo())) entity.setCellGSTInvoiceNo(req.getCellGSTInvoiceNo());
        if (differs(entity.getMeterMake(), req.getMeterMake())) entity.setMeterMake(req.getMeterMake());
        if (differs(entity.getMeterNumber(), req.getMeterNumber())) entity.setMeterNumber(req.getMeterNumber());
        if (differs(entity.getNetMeterNumber(), req.getNetMeterNumber())) entity.setNetMeterNumber(req.getNetMeterNumber());
        if (differs(entity.getInvoiceNumber(), req.getInvoiceNumber())) entity.setInvoiceNumber(req.getInvoiceNumber());
        if (differs(entity.getTotalAmountIncludingGST(), req.getTotalAmountIncludingGST())) entity.setTotalAmountIncludingGST(req.getTotalAmountIncludingGST());
        if (differs(entity.getYearOfManufacturing(), req.getYearOfManufacturing())) entity.setYearOfManufacturing(req.getYearOfManufacturing());

        if (imageFieldTouched(req.getConsumerSignature(), req.getExistingConsumerSignature())) {
            entity.setConsumerSignature(
                    syncImages(entity.getConsumerSignature(), req.getConsumerSignature(),
                            req.getExistingConsumerSignature(), "consumerSignatures"));
        }
        if (imageFieldTouched(req.getAadharImages(), req.getExistingAadharImages())) {
            entity.setAadharImages(
                    syncImages(entity.getAadharImages(), req.getAadharImages(),
                            req.getExistingAadharImages(), "aadharImages"));
        }
        if (imageFieldTouched(req.getSitePhotos(), req.getExistingSitePhotos())) {
            entity.setSitePhotos(
                    syncImages(entity.getSitePhotos(), req.getSitePhotos(),
                            req.getExistingSitePhotos(), "sitePhotos"));
        }
        if (imageFieldTouched(req.getNetMeteringStamp(), req.getExistingNetMeteringStamp())) {
            entity.setNetMeteringStamp(
                    syncImages(entity.getNetMeteringStamp(), req.getNetMeteringStamp(),
                            req.getExistingNetMeteringStamp(), "netMeteringStamps"));
        }
        if (imageFieldTouched(req.getAnnexureTwoStamp(), req.getExistingAnnexureTwoStamp())) {
            entity.setAnnexureTwoStamp(
                    syncImages(entity.getAnnexureTwoStamp(), req.getAnnexureTwoStamp(),
                            req.getExistingAnnexureTwoStamp(), "annexureTwoStamps"));
        }
    }

    private boolean differs(Object current, Object incoming) {
        if (incoming == null) return false;
        return !Objects.equals(current, incoming);
    }

    private boolean imageFieldTouched(List<MultipartFile> newFiles, List<String> existingUrlsFromRequest) {
        boolean hasNewFiles = newFiles != null && !newFiles.isEmpty();
        boolean hasExistingUrls = existingUrlsFromRequest != null && !existingUrlsFromRequest.isEmpty();
        return hasNewFiles || hasExistingUrls;
    }

    private SolarRecordResponseDto mapToResponse(SolarRecord entity) {
        SolarRecordResponseDto response = new SolarRecordResponseDto();
        modelMapper.map(entity, response);
        return response;
    }

    private List<String> uploadImages(List<MultipartFile> files, String folder) {
        if (files == null || files.isEmpty()) return Collections.emptyList();
        List<MultipartFile> validFiles = files.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (validFiles.isEmpty()) return Collections.emptyList();

        List<CompletableFuture<String>> futures = validFiles.stream()
                .map(file -> CompletableFuture.supplyAsync(
                        () -> cloudinaryService.uploadFile(file, folder), cloudinaryPool))
                .collect(Collectors.toList());

        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    private List<String> syncImages(List<String> currentDbImages,
                                    List<MultipartFile> newFiles,
                                    List<String> existingUrlsFromRequest,
                                    String folder) {

        List<String> keptUrls = existingUrlsFromRequest != null
                ? existingUrlsFromRequest
                : (currentDbImages != null ? currentDbImages : Collections.emptyList());

        List<String> finalUrls = new ArrayList<>(keptUrls);

        if (newFiles != null && !newFiles.isEmpty()) {
            finalUrls.addAll(uploadImages(newFiles, folder));
        }

        if (currentDbImages != null && !currentDbImages.isEmpty()) {
            Set<String> keepSet = Set.copyOf(finalUrls);
            List<String> urlsToDelete = currentDbImages.stream()
                    .filter(url -> !keepSet.contains(url))
                    .collect(Collectors.toList());
            if (!urlsToDelete.isEmpty()) {
                cloudinaryService.deleteFiles(urlsToDelete);
            }
        }

        return finalUrls;
    }
}