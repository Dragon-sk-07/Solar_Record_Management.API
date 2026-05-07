package com.suraj.Customer_Portal_29.service;

import com.suraj.Customer_Portal_29.dto.request.SolarRecordRequestDto;
import com.suraj.Customer_Portal_29.dto.response.SolarRecordResponseDto;
import com.suraj.Customer_Portal_29.entity.SolarRecord;
import com.suraj.Customer_Portal_29.repository.SolarRecordRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
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

    public SolarRecordService(SolarRecordRepository repository,
                              ModelMapper modelMapper,
                              CloudinaryService cloudinaryService) {
        this.repository = repository;
        this.modelMapper = modelMapper;
        this.cloudinaryService = cloudinaryService;
    }

    public SolarRecordResponseDto save(SolarRecordRequestDto request) {
        SolarRecord entity = mapToEntity(request);
        SolarRecord saved = repository.save(entity);
        return mapToResponse(saved);
    }

    public List<SolarRecordResponseDto> findAll() {
        return repository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "solarRecords", key = "#id")
    public SolarRecordResponseDto findById(String id) {
        SolarRecord entity = findEntityById(id);
        return mapToResponse(entity);
    }

    @CacheEvict(value = "solarRecords", key = "#id")
    public SolarRecordResponseDto update(String id, SolarRecordRequestDto request) {
        SolarRecord existing = findEntityById(id);
        updateEntity(existing, request);
        SolarRecord updated = repository.save(existing);
        return mapToResponse(updated);
    }

    public void delete(String id) {
        SolarRecord entity = findEntityById(id);

        if (entity.getSitePhotos() != null && !entity.getSitePhotos().isEmpty()) {
            cloudinaryService.deleteFiles(entity.getSitePhotos());
        }
        if (entity.getAadharImages() != null && !entity.getAadharImages().isEmpty()) {
            cloudinaryService.deleteFiles(entity.getAadharImages());
        }
        if (entity.getVendorSignature() != null && !entity.getVendorSignature().isEmpty()) {
            cloudinaryService.deleteFiles(entity.getVendorSignature());
        }
        if (entity.getConsumerSignature() != null && !entity.getConsumerSignature().isEmpty()) {
            cloudinaryService.deleteFiles(entity.getConsumerSignature());
        }
        if (entity.getMsedclSignature() != null && !entity.getMsedclSignature().isEmpty()) {
            cloudinaryService.deleteFiles(entity.getMsedclSignature());
        }
        if (entity.getVendorStamp() != null && !entity.getVendorStamp().isEmpty()) {
            cloudinaryService.deleteFiles(entity.getVendorStamp());
        }
        if (entity.getWitnessSignature() != null && !entity.getWitnessSignature().isEmpty()) {
            cloudinaryService.deleteFiles(entity.getWitnessSignature());
        }

        repository.delete(entity);
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

        List<String> vendorSignatureList = mergeImageLists(null, req.getVendorSignature(), "vendorSignatures");
        entity.setVendorSignature(vendorSignatureList);

        List<String> consumerSignatureList = mergeImageLists(null, req.getConsumerSignature(), "consumerSignatures");
        entity.setConsumerSignature(consumerSignatureList);

        List<String> msedclSignatureList = mergeImageLists(null, req.getMsedclSignature(), "msedclSignatures");
        entity.setMsedclSignature(msedclSignatureList);

        List<String> vendorStampList = mergeImageLists(null, req.getVendorStamp(), "vendorStamps");
        entity.setVendorStamp(vendorStampList);

        List<String> witnessSignatureList = mergeImageLists(null, req.getWitnessSignature(), "witnessSignatures");
        entity.setWitnessSignature(witnessSignatureList);

        List<String> photoPaths = mergeImageLists(null, req.getSitePhotos(), "sitePhotos");
        entity.setSitePhotos(photoPaths);

        List<String> aadharPaths = mergeImageLists(null, req.getAadharImages(), "aadharImages");
        entity.setAadharImages(aadharPaths);

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

        List<String> updatedVendorSignature = mergeImageLists(req.getExistingVendorSignature(), req.getVendorSignature(), "vendorSignatures");
        entity.setVendorSignature(updatedVendorSignature);

        List<String> updatedConsumerSignature = mergeImageLists(req.getExistingConsumerSignature(), req.getConsumerSignature(), "consumerSignatures");
        entity.setConsumerSignature(updatedConsumerSignature);

        List<String> updatedMsedclSignature = mergeImageLists(req.getExistingMsedclSignature(), req.getMsedclSignature(), "msedclSignatures");
        entity.setMsedclSignature(updatedMsedclSignature);

        List<String> updatedVendorStamp = mergeImageLists(req.getExistingVendorStamp(), req.getVendorStamp(), "vendorStamps");
        entity.setVendorStamp(updatedVendorStamp);

        List<String> updatedWitnessSignature = mergeImageLists(req.getExistingWitnessSignature(), req.getWitnessSignature(), "witnessSignatures");
        entity.setWitnessSignature(updatedWitnessSignature);

        List<String> updatedPhotos = mergeImageLists(req.getExistingSitePhotos(), req.getSitePhotos(), "sitePhotos");
        entity.setSitePhotos(updatedPhotos);

        List<String> updatedAadharImages = mergeImageLists(req.getExistingAadharImages(), req.getAadharImages(), "aadharImages");
        entity.setAadharImages(updatedAadharImages);
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

        List<String> vendorSignatureList = mergeImageLists(null, req.getVendorSignature(), "vendorSignatures");
        entity.setVendorSignature(vendorSignatureList);

        List<String> vendorStampList = mergeImageLists(null, req.getVendorStamp(), "vendorStamps");
        entity.setVendorStamp(vendorStampList);
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
        return response;
    }

    private List<String> uploadImagesWithCompression(List<MultipartFile> files, String folder) {
        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file != null) {
                String url = cloudinaryService.uploadFile(file, folder);
                result.add(url);
                System.gc(); // Hint to free memory after each upload
            }
        }
        return result;
    }

    private List<String> mergeImageLists(List<String> existing, List<MultipartFile> newFiles, String folder) {
        List<String> result = new ArrayList<>();
        if (existing != null) {
            result.addAll(existing);
        }
        if (newFiles != null && !newFiles.isEmpty()) {
            result.addAll(uploadImagesWithCompression(newFiles, folder));
        }
        return result;
    }

    private void setImageList(List<String> existing, List<MultipartFile> newFiles,
                              java.util.function.Consumer<List<String>> setter, String folder) {
        List<String> result = new ArrayList<>();
        if (existing != null) {
            result.addAll(existing);
        }
        if (newFiles != null && !newFiles.isEmpty()) {
            result.addAll(uploadImagesWithCompression(newFiles, folder));
        }
        setter.accept(result);
    }
    private void deleteRemovedImages(List<String> oldImages, List<String> newImages) {
        if (oldImages == null || oldImages.isEmpty()) return;
        if (newImages == null) {
            cloudinaryService.deleteFiles(oldImages);
            return;
        }
        List<String> toDelete = new ArrayList<>(oldImages);
        toDelete.removeAll(newImages);
        cloudinaryService.deleteFiles(toDelete);
    }
}