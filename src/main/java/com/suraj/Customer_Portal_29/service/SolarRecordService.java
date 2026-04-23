package com.suraj.Customer_Portal_29.service;

import com.suraj.Customer_Portal_29.dto.request.SolarRecordRequestDto;
import com.suraj.Customer_Portal_29.dto.response.SolarRecordResponseDto;
import com.suraj.Customer_Portal_29.entity.SolarRecord;
import com.suraj.Customer_Portal_29.repository.SolarRecordRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SolarRecordService {


    private final SolarRecordRepository repository;
    private final ModelMapper modelMapper;

    public SolarRecordService(SolarRecordRepository repository,
                              ModelMapper modelMapper) {
        this.repository = repository;
        this.modelMapper = modelMapper;
    }
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";




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

        List<String> photoPaths = savePhotos(req.getSitePhotos());
        entity.setSitePhotos(photoPaths);

        // DEBUG - Add these lines
        System.out.println("=== AADHAR DEBUG IN MAP TO ENTITY ===");
        System.out.println("req.getAadharImage(): " + req.getAadharImage());
        System.out.println("req.getExistingAadharImage(): " + req.getExistingAadharImage());

        if (req.getAadharImage() != null && !req.getAadharImage().isEmpty()) {
            System.out.println("Saving new Aadhar image: " + req.getAadharImage().getOriginalFilename());
            entity.setAadharImagePath(saveAadharImage(req.getAadharImage()));
        } else if (req.getExistingAadharImage() != null && !req.getExistingAadharImage().isEmpty()) {
            System.out.println("Using existing Aadhar image: " + req.getExistingAadharImage());
            entity.setAadharImagePath(req.getExistingAadharImage());
        } else {
            System.out.println("NO AADHAR IMAGE PROVIDED - Both are null/empty");
        }

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

        List<String> updatedPhotos = new ArrayList<>();

        if (req.getExistingPhotos() != null) {
            updatedPhotos.addAll(req.getExistingPhotos());
        }

        if (req.getAadharImage() != null && !req.getAadharImage().isEmpty()) {
            entity.setAadharImagePath(saveAadharImage(req.getAadharImage()));
        } else if (req.getExistingAadharImage() != null && !req.getExistingAadharImage().isEmpty()) {
            entity.setAadharImagePath(req.getExistingAadharImage());
        }

        if (req.getSitePhotos() != null && !req.getSitePhotos().isEmpty()) {
            updatedPhotos.addAll(savePhotos(req.getSitePhotos()));
        }

        entity.setSitePhotos(
                updatedPhotos.stream().distinct().collect(Collectors.toList())
        );
    }

    private void mapBasicFields(SolarRecord entity, SolarRecordRequestDto req) {
        entity.setName(req.getName());
        entity.setConsumerNumber(req.getConsumerNumber());
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

    private SolarRecordResponseDto mapToResponse(SolarRecord entity) {
        SolarRecordResponseDto response = new SolarRecordResponseDto();
        modelMapper.map(entity, response);
        response.setAadharImagePath(entity.getAadharImagePath());
        System.out.println("DEBUG - Entity path: " + entity.getAadharImagePath());
        System.out.println("DEBUG - Response path: " + response.getAadharImagePath());
        return response;
    }

    private String saveAadharImage(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        try {
            String originalExt = file.getOriginalFilename();
            String ext = originalExt != null && originalExt.contains(".") ? originalExt.substring(originalExt.lastIndexOf(".")) : ".jpg";
            String fileName = "aadhar_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + ext;

            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);

            String savedPath = "/uploads/" + fileName;
            System.out.println("✅ Aadhar saved: " + savedPath);
            return savedPath;
        } catch (Exception e) {
            System.err.println("❌ Aadhar save failed: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
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
        try {
            String timestamp = String.valueOf(System.currentTimeMillis());
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ?
                    originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String fileName = timestamp + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;

            Path uploadPath = Paths.get(UPLOAD_DIR);
            Path filePath = uploadPath.resolve(fileName);

            Files.createDirectories(uploadPath);
            Files.write(filePath, file.getBytes());

            return "/uploads/" + fileName;

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload photo: " + e.getMessage(), e);
        }
    }
}