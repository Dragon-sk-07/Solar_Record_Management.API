package com.suraj.Customer_Portal_29.service;

import com.suraj.Customer_Portal_29.dto.request.SolarRecordRequestDto;
import com.suraj.Customer_Portal_29.dto.response.SolarRecordResponseDto;
import com.suraj.Customer_Portal_29.entity.SolarRecord;
import com.suraj.Customer_Portal_29.entity.Owner;
import com.suraj.Customer_Portal_29.entity.UserRole;
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

    public SolarRecordService(SolarRecordRepository repository,
                              ModelMapper modelMapper,
                              CloudinaryService cloudinaryService,
                              OwnerRepository ownerRepository) {
        this.repository = repository;
        this.modelMapper = modelMapper;
        this.cloudinaryService = cloudinaryService;
        this.ownerRepository = ownerRepository;
    }

    private Owner getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ownerRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private boolean isSuperAdmin(Owner user) {
        return user.getRole() == UserRole.SUPER_ADMIN;
    }

    private boolean canModifyRecord(Owner user, SolarRecord record) {
        return isSuperAdmin(user) || record.getCreatedByUserEmail().equals(user.getEmail());
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
        List<SolarRecord> records = isSuperAdmin(currentUser)
                ? repository.findAll()
                : repository.findByCreatedByUserEmail(currentUser.getEmail());
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
        if (!canModifyRecord(currentUser, existing)) {
            throw new RuntimeException("No permission to update this record");
        }
        updateEntity(existing, request);
        return mapToResponse(repository.save(existing));
    }

    public void delete(String id) {
        Owner currentUser = getCurrentUser();
        SolarRecord entity = findEntityById(id);
        if (!canModifyRecord(currentUser, entity)) {
            throw new RuntimeException("No permission to delete this record");
        }
        deleteCloudinaryFiles(entity);
        repository.delete(entity);
    }

    private void deleteCloudinaryFiles(SolarRecord entity) {
        List<List<String>> fileLists = Arrays.asList(
                entity.getSitePhotos(), entity.getAadharImages(),
                entity.getVendorSignature(), entity.getConsumerSignature(),
                entity.getWitnessSignature(), entity.getNetMeteringStamp(),
                entity.getMsedclSignature(),
                entity.getHeaderLogo(),
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
        entity.setVendorName(req.getVendorName());
        entity.setVendorAddress(req.getVendorAddress());
        entity.setVendorMobile(req.getVendorMobile());
        entity.setVendorEmail(req.getVendorEmail());
        entity.setAuthorizedPersonName(req.getAuthorizedPersonName());
        entity.setMsedclOfficerName(req.getMsedclOfficerName());
        entity.setWitness1Name(req.getWitness1Name());
        entity.setWitness1Address(req.getWitness1Address());
        entity.setWitness2Name(req.getWitness2Name());
        entity.setWitness2Address(req.getWitness2Address());
        entity.setCellManufacturerName(req.getCellManufacturerName());
        entity.setCellGSTInvoiceNo(req.getCellGSTInvoiceNo());
        entity.setMeterMake(req.getMeterMake());
        entity.setMeterNumber(req.getMeterNumber());
        entity.setNetMeterNumber(req.getNetMeterNumber());
        entity.setInvoiceNumber(req.getInvoiceNumber());
        entity.setYearOfManufacturing(req.getYearOfManufacturing());
        entity.setHeaderLogo(mergeImageLists(null, req.getHeaderLogo(), "headerLogos"));
        entity.setVendorSignature(mergeImageLists(null, req.getVendorSignature(), "vendorSignatures"));
        entity.setConsumerSignature(mergeImageLists(null, req.getConsumerSignature(), "consumerSignatures"));
        entity.setMsedclSignature(mergeImageLists(null, req.getMsedclSignature(), "msedclSignatures"));
        entity.setWitnessSignature(mergeImageLists(null, req.getWitnessSignature(), "witnessSignatures"));
        entity.setAadharImages(mergeImageLists(null, req.getAadharImages(), "aadharImages"));
        entity.setSitePhotos(mergeImageLists(null, req.getSitePhotos(), "sitePhotos"));
        entity.setNetMeteringStamp(mergeImageLists(null, req.getNetMeteringStamp(), "netMeteringStamps"));
        entity.setAnnexureTwoStamp(mergeImageLists(null, req.getAnnexureTwoStamp(), "annexureTwoStamps"));
        return entity;
    }

    private void updateEntity(SolarRecord entity, SolarRecordRequestDto req) {
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
        entity.setVendorName(req.getVendorName());
        entity.setVendorAddress(req.getVendorAddress());
        entity.setVendorMobile(req.getVendorMobile());
        entity.setVendorEmail(req.getVendorEmail());
        entity.setAuthorizedPersonName(req.getAuthorizedPersonName());
        entity.setMsedclOfficerName(req.getMsedclOfficerName());
        entity.setWitness1Name(req.getWitness1Name());
        entity.setWitness1Address(req.getWitness1Address());
        entity.setWitness2Name(req.getWitness2Name());
        entity.setWitness2Address(req.getWitness2Address());
        entity.setCellManufacturerName(req.getCellManufacturerName());
        entity.setCellGSTInvoiceNo(req.getCellGSTInvoiceNo());
        entity.setMeterMake(req.getMeterMake());
        entity.setMeterNumber(req.getMeterNumber());
        entity.setNetMeterNumber(req.getNetMeterNumber());
        entity.setInvoiceNumber(req.getInvoiceNumber());
        entity.setYearOfManufacturing(req.getYearOfManufacturing());
        entity.setHeaderLogo(mergeImageLists(req.getExistingHeaderLogo(), req.getHeaderLogo(), "headerLogos"));
        entity.setVendorSignature(mergeImageLists(req.getExistingVendorSignature(), req.getVendorSignature(), "vendorSignatures"));
        entity.setConsumerSignature(mergeImageLists(req.getExistingConsumerSignature(), req.getConsumerSignature(), "consumerSignatures"));
        entity.setMsedclSignature(mergeImageLists(req.getExistingMsedclSignature(), req.getMsedclSignature(), "msedclSignatures"));
        entity.setWitnessSignature(mergeImageLists(req.getExistingWitnessSignature(), req.getWitnessSignature(), "witnessSignatures"));
        entity.setAadharImages(mergeImageLists(req.getExistingAadharImages(), req.getAadharImages(), "aadharImages"));
        entity.setSitePhotos(mergeImageLists(req.getExistingSitePhotos(), req.getSitePhotos(), "sitePhotos"));
        entity.setNetMeteringStamp(mergeImageLists(req.getExistingNetMeteringStamp(), req.getNetMeteringStamp(), "netMeteringStamps"));
        entity.setAnnexureTwoStamp(mergeImageLists(req.getExistingAnnexureTwoStamp(), req.getAnnexureTwoStamp(), "annexureTwoStamps"));
    }

    private SolarRecordResponseDto mapToResponse(SolarRecord entity) {
        SolarRecordResponseDto response = new SolarRecordResponseDto();
        modelMapper.map(entity, response);
        response.setAadharImages(entity.getAadharImages());
        response.setSitePhotos(entity.getSitePhotos());
        response.setVendorSignature(entity.getVendorSignature());
        response.setConsumerSignature(entity.getConsumerSignature());
        response.setMsedclSignature(entity.getMsedclSignature());
        response.setWitnessSignature(entity.getWitnessSignature());
        response.setNetMeteringStamp(entity.getNetMeteringStamp());
        response.setAnnexureTwoStamp(entity.getAnnexureTwoStamp());
        response.setCellManufacturerName(entity.getCellManufacturerName());
        response.setCellGSTInvoiceNo(entity.getCellGSTInvoiceNo());
        response.setMeterMake(entity.getMeterMake());
        response.setMeterNumber(entity.getMeterNumber());
        response.setNetMeterNumber(entity.getNetMeterNumber());
        response.setInvoiceNumber(entity.getInvoiceNumber());
        response.setYearOfManufacturing(entity.getYearOfManufacturing());
        response.setHeaderLogo(entity.getHeaderLogo());
        return response;
    }

    private List<String> uploadImagesWithCompression(List<MultipartFile> files, String folder) {
        if (files == null || files.isEmpty()) return Collections.emptyList();
        return files.stream()
                .filter(Objects::nonNull)
                .map(file -> cloudinaryService.uploadFile(file, folder))
                .collect(Collectors.toList());
    }

    private List<String> mergeImageLists(List<String> existing, List<MultipartFile> newFiles, String folder) {
        List<String> result = new ArrayList<>();
        if (existing != null && !existing.isEmpty()) result.addAll(existing);
        if (newFiles != null && !newFiles.isEmpty()) result.addAll(uploadImagesWithCompression(newFiles, folder));
        return result;
    }
}