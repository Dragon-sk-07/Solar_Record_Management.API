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

        List<String> photoPaths = savePhotos(req.getSitePhotos());
        entity.setSitePhotos(photoPaths);

        List<String> aadharPaths = saveAadharImages(req.getAadharImages());
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

        // Handle Site Photos
        List<String> updatedPhotos = new ArrayList<>();
        if (req.getExistingPhotos() != null) {
            updatedPhotos.addAll(req.getExistingPhotos());
        }
        if (req.getSitePhotos() != null && !req.getSitePhotos().isEmpty()) {
            updatedPhotos.addAll(savePhotos(req.getSitePhotos()));
        }
        entity.setSitePhotos(updatedPhotos.stream().distinct().collect(Collectors.toList()));

        // Handle Aadhar Images - MERGE existing + new (SAME LOGIC AS SITE PHOTOS)
        List<String> updatedAadharImages = new ArrayList<>();
        if (req.getExistingAadharImages() != null && !req.getExistingAadharImages().isEmpty()) {
            updatedAadharImages.addAll(req.getExistingAadharImages());
        }
        if (req.getAadharImages() != null && !req.getAadharImages().isEmpty()) {
            updatedAadharImages.addAll(saveAadharImages(req.getAadharImages()));
        }
        entity.setAadharImages(updatedAadharImages.isEmpty() ? null : updatedAadharImages);
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
        entity.setVendorStamp(req.getVendorStamp());
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

    private SolarRecordResponseDto mapToResponse(SolarRecord entity) {
        SolarRecordResponseDto response = new SolarRecordResponseDto();
        modelMapper.map(entity, response);
        response.setAadharImages(entity.getAadharImages());
        return response;
    }

    private List<String> saveAadharImages(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }
        return files.stream()
                .map(this::saveSingleAadharImage)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    private String saveSingleAadharImage(MultipartFile file) {
        return cloudinaryService.uploadFile(file, "aadharImages");
    }

    private List<String> savePhotos(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }

        return files.stream()
                .map(this::saveSinglePhoto)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private String saveSinglePhoto(MultipartFile file) {
        return cloudinaryService.uploadFile(file, "sitePhotos");
    }
}