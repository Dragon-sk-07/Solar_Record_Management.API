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
        org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        String email = auth.getName();
        if (email == null || email.isEmpty()) {
            throw new RuntimeException("Email not found in authentication");
        }
        return ownerRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
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
                entity.getMsedclSignature(), entity.getVendorStamp(),
                entity.getNetMeteringStamp(),
                entity.getWitnessSignature()
        );

        fileLists.stream()
                .filter(Objects::nonNull)
                .forEach(cloudinaryService::deleteFiles);
    }

    private SolarRecord findEntityById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solar record not found with id: " + id));
    }

    private SolarRecord mapToEntity(SolarRecordRequestDto req) {
        SolarRecord entity = new SolarRecord();
        mapBasicFields(entity, req);
        mapSanctionFields(entity, req);
        mapReArrangementFields(entity, req);
        mapModuleFields(entity, req);
        mapWarrantyFields(entity, req);
        mapInverterFields(entity, req);
        mapEarthingFields(entity, req);
        mapVendorFields(entity, req);
        mapMsedclFields(entity, req);
        mapAgreementFields(entity, req);
        mapApplicationFields(entity, req);
        mapWitnessFields(entity, req);
        mapIndemnityFields(entity, req);
        mapInspectionFields(entity, req);

        entity.setVendorSignature(mergeImageLists(null, req.getVendorSignature(), "vendorSignatures"));
        entity.setConsumerSignature(mergeImageLists(null, req.getConsumerSignature(), "consumerSignatures"));
        entity.setMsedclSignature(mergeImageLists(null, req.getMsedclSignature(), "msedclSignatures"));
        entity.setVendorStamp(mergeImageLists(null, req.getVendorStamp(), "vendorStamps"));
        entity.setWitnessSignature(mergeImageLists(null, req.getWitnessSignature(), "witnessSignatures"));
        entity.setSitePhotos(mergeImageLists(null, req.getSitePhotos(), "sitePhotos"));
        entity.setAadharImages(mergeImageLists(null, req.getAadharImages(), "aadharImages"));
        entity.setNetMeteringStamp(mergeImageLists(null, req.getNetMeteringStamp(), "netMeteringStamps"));

        return entity;
    }

    private void updateEntity(SolarRecord entity, SolarRecordRequestDto req) {
        mapBasicFields(entity, req);
        mapSanctionFields(entity, req);
        mapReArrangementFields(entity, req);
        mapModuleFields(entity, req);
        mapWarrantyFields(entity, req);
        mapInverterFields(entity, req);
        mapEarthingFields(entity, req);
        mapVendorFields(entity, req);
        mapMsedclFields(entity, req);
        mapAgreementFields(entity, req);
        mapApplicationFields(entity, req);
        mapWitnessFields(entity, req);
        mapIndemnityFields(entity, req);
        mapInspectionFields(entity, req);

        entity.setVendorSignature(mergeImageLists(req.getExistingVendorSignature(), req.getVendorSignature(), "vendorSignatures"));
        entity.setConsumerSignature(mergeImageLists(req.getExistingConsumerSignature(), req.getConsumerSignature(), "consumerSignatures"));
        entity.setMsedclSignature(mergeImageLists(req.getExistingMsedclSignature(), req.getMsedclSignature(), "msedclSignatures"));
        entity.setVendorStamp(mergeImageLists(req.getExistingVendorStamp(), req.getVendorStamp(), "vendorStamps"));
        entity.setWitnessSignature(mergeImageLists(req.getExistingWitnessSignature(), req.getWitnessSignature(), "witnessSignatures"));
        entity.setSitePhotos(mergeImageLists(req.getExistingSitePhotos(), req.getSitePhotos(), "sitePhotos"));
        entity.setAadharImages(mergeImageLists(req.getExistingAadharImages(), req.getAadharImages(), "aadharImages"));
        entity.setNetMeteringStamp(mergeImageLists(req.getExistingNetMeteringStamp(), req.getNetMeteringStamp(), "netMeteringStamps"));
    }

    private void mapBasicFields(SolarRecord entity, SolarRecordRequestDto req) {
        entity.setName(req.getName());
        entity.setConsumerNumber(req.getConsumerNumber());
        entity.setMeterNumber(req.getMeterNumber());
        entity.setNetMeterNumber(req.getNetMeterNumber());
        entity.setMobileNumber(req.getMobileNumber());
        entity.setEmail(req.getEmail());
        entity.setSiteAddress(req.getSiteAddress());
        entity.setCategory(req.getCategory());
        entity.setAadharNumber(req.getAadharNumber());
    }

    private void mapSanctionFields(SolarRecord entity, SolarRecordRequestDto req) {
        entity.setSanctionNumber(req.getSanctionNumber());
        entity.setSanctionedCapacity(req.getSanctionedCapacity());
        entity.setInstalledCapacity(req.getInstalledCapacity());
    }

    private void mapReArrangementFields(SolarRecord entity, SolarRecordRequestDto req) {
        entity.setReArrangementType(req.getReArrangementType());
        entity.setReSource(req.getReSource());
        entity.setCapacityType(req.getCapacityType());
        entity.setProjectModel(req.getProjectModel());
        entity.setReInstalledCapacityRooftop(req.getReInstalledCapacityRooftop());
        entity.setReInstalledCapacityRooftopGround(req.getReInstalledCapacityRooftopGround());
        entity.setReInstalledCapacityGround(req.getReInstalledCapacityGround());
        entity.setInstallationDate(req.getInstallationDate());
    }

    private void mapModuleFields(SolarRecord entity, SolarRecordRequestDto req) {
        entity.setModuleMake(req.getModuleMake());
        entity.setAlmmModelNumber(req.getAlmmModelNumber());
        entity.setWattagePerModule(req.getWattagePerModule());
        entity.setNumberOfModules(req.getNumberOfModules());
        entity.setTotalCapacityKWP(req.getTotalCapacityKWP());
        entity.setModuleSerialNumbers(req.getModuleSerialNumbers());
        entity.setCellManufacturerName(req.getCellManufacturerName());
        entity.setCellGSTInvoiceNo(req.getCellGSTInvoiceNo());
    }

    private void mapWarrantyFields(SolarRecord entity, SolarRecordRequestDto req) {
        entity.setProductWarranty(req.getProductWarranty());
        entity.setPerformanceWarranty(req.getPerformanceWarranty());
    }

    private void mapInverterFields(SolarRecord entity, SolarRecordRequestDto req) {
        entity.setInverterMake(req.getInverterMake());
        entity.setInverterModelNumber(req.getInverterModelNumber());
        entity.setInverterRating(req.getInverterRating());
        entity.setInverterCapacity(req.getInverterCapacity());
        entity.setChargeControllerType(req.getChargeControllerType());
        entity.setMpptCapacity(req.getMpptCapacity());
        entity.setHpd(req.getHpd());
        entity.setYearOfManufacturing(req.getYearOfManufacturing());
    }

    private void mapEarthingFields(SolarRecord entity, SolarRecordRequestDto req) {
        entity.setNumberOfEarthings(req.getNumberOfEarthings());
        entity.setEarthResistance(req.getEarthResistance());
        entity.setLighteningArrester(req.getLighteningArrester());
    }

    private void mapVendorFields(SolarRecord entity, SolarRecordRequestDto req) {
        entity.setVendorName(req.getVendorName());
        entity.setVendorAddress(req.getVendorAddress());
        entity.setAuthorizedPersonName(req.getAuthorizedPersonName());
        entity.setDesignation(req.getDesignation());
    }

    private void mapMsedclFields(SolarRecord entity, SolarRecordRequestDto req) {
        entity.setMsedclAddress(req.getMsedclAddress());
        entity.setMsedclOfficerName(req.getMsedclOfficerName());
        entity.setMsedclOfficerDesignation(req.getMsedclOfficerDesignation());
        entity.setInspectorName(req.getInspectorName());
    }

    private void mapAgreementFields(SolarRecord entity, SolarRecordRequestDto req) {
        entity.setLocation(req.getLocation());
        entity.setDay(req.getDay());
        entity.setMonth(req.getMonth());
        entity.setYear(req.getYear());
        entity.setInterconnectionPoint(req.getInterconnectionPoint());
    }

    private void mapApplicationFields(SolarRecord entity, SolarRecordRequestDto req) {
        entity.setApplicationNumber(req.getApplicationNumber());
        entity.setApplicationDate(req.getApplicationDate());
        entity.setDiscomName(req.getDiscomName());
        entity.setPlace(req.getPlace());
    }

    private void mapWitnessFields(SolarRecord entity, SolarRecordRequestDto req) {
        entity.setWitness1Name(req.getWitness1Name());
        entity.setWitness1Address(req.getWitness1Address());
        entity.setWitness2Name(req.getWitness2Name());
        entity.setWitness2Address(req.getWitness2Address());
    }

    private void mapIndemnityFields(SolarRecord entity, SolarRecordRequestDto req) {
        entity.setIndemnityDay(req.getIndemnityDay());
        entity.setIndemnityMonth(req.getIndemnityMonth());
        entity.setIndemnityYear(req.getIndemnityYear());
        entity.setGrReferenceNumber(req.getGrReferenceNumber());
        entity.setGrReferenceDate(req.getGrReferenceDate());
        entity.setPbgAmount(req.getPbgAmount());
    }

    private void mapInspectionFields(SolarRecord entity, SolarRecordRequestDto req) {
        entity.setMeterMake(req.getMeterMake());
        entity.setAcCapacityCheck(req.getAcCapacityCheck());
        entity.setArrearsStatus(req.getArrearsStatus());
        entity.setSldStatus(req.getSldStatus());
        entity.setLayoutStatus(req.getLayoutStatus());
        entity.setEarthingDiagram(req.getEarthingDiagram());
        entity.setEquipmentList(req.getEquipmentList());
        entity.setIslandingCertificate(req.getIslandingCertificate());
        entity.setEarthingLA(req.getEarthingLA());
        entity.setEarthingPanel(req.getEarthingPanel());
        entity.setEarthingDCBB(req.getEarthingDCBB());
        entity.setEarthingACBB(req.getEarthingACBB());
        entity.setEarthingInverter(req.getEarthingInverter());
        entity.setEarthingMetering(req.getEarthingMetering());
        entity.setMetallicEarthed(req.getMetallicEarthed());
        entity.setDcFuses(req.getDcFuses());
        entity.setAcSurge(req.getAcSurge());
        entity.setAcdbSurge(req.getAcdbSurge());
        entity.setIsolationSwitchStatus(req.getIsolationSwitchStatus());
        entity.setMcbLoad(req.getMcbLoad());
        entity.setIslandingCheck(req.getIslandingCheck());
        entity.setIslandingSatisfactory(req.getIslandingSatisfactory());
        entity.setBackupCheck(req.getBackupCheck());
        entity.setGenMeterConn(req.getGenMeterConn());
        entity.setNetMeterConn(req.getNetMeterConn());
        entity.setInverterHealthy(req.getInverterHealthy());
        entity.setSystemTakeover(req.getSystemTakeover());
        entity.setMccbRating(req.getMccbRating());
        entity.setMeteringRCCB(req.getMeteringRCCB());
        entity.setNetInstalled(req.getNetInstalled());
        entity.setNetTesting(req.getNetTesting());
        entity.setGenInstalled(req.getGenInstalled());
        entity.setGenTesting(req.getGenTesting());
    }

    private SolarRecordResponseDto mapToResponse(SolarRecord entity) {
        SolarRecordResponseDto response = new SolarRecordResponseDto();
        modelMapper.map(entity, response);
        response.setAadharImages(entity.getAadharImages());
        response.setSitePhotos(entity.getSitePhotos());
        response.setVendorSignature(entity.getVendorSignature());
        response.setConsumerSignature(entity.getConsumerSignature());
        response.setMsedclSignature(entity.getMsedclSignature());
        response.setVendorStamp(entity.getVendorStamp());
        response.setWitnessSignature(entity.getWitnessSignature());
        response.setNetMeteringStamp(entity.getNetMeteringStamp());
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