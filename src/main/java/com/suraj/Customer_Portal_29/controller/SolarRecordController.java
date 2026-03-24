package com.suraj.Customer_Portal_29.controller;

import com.suraj.Customer_Portal_29.dto.request.SolarRecordRequestDto;
import com.suraj.Customer_Portal_29.dto.response.ApiResponseDto;
import com.suraj.Customer_Portal_29.dto.response.SolarRecordResponseDto;
import com.suraj.Customer_Portal_29.service.SolarRecordService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:2929")
@RequestMapping("/solar-records")
public class SolarRecordController {

    private final SolarRecordService service;

    public SolarRecordController(SolarRecordService service) {
        this.service = service;
    }

    // ================= CREATE =================
    @PostMapping(consumes = "multipart/form-data")
    public ApiResponseDto<SolarRecordResponseDto> save(
            @Valid @ModelAttribute SolarRecordRequestDto request
    ){

        SolarRecordResponseDto data = service.save(request);

        return new ApiResponseDto<>(
                "Solar record created successfully",
                data
        );
    }


    // ================= READ ALL =================
    @GetMapping
    public ApiResponseDto<List<SolarRecordResponseDto>> getAll() {

        List<SolarRecordResponseDto> data = service.findAll();

        return new ApiResponseDto<>(
                "Solar records fetched successfully",
                data
        );
    }


    // ================= READ BY ID =================
    @GetMapping("/{id}")
    public ApiResponseDto<SolarRecordResponseDto> getById(
            @PathVariable String id
    ) {

        SolarRecordResponseDto data = service.findById(id);

        return new ApiResponseDto<>(
                "Solar record fetched successfully",
                data
        );
    }


    // ================= UPDATE =================
    @PutMapping(value="/{id}", consumes="multipart/form-data")
    public ApiResponseDto<SolarRecordResponseDto> update(
            @PathVariable String id,
            @Valid @ModelAttribute SolarRecordRequestDto request
    ) {

        SolarRecordResponseDto data = service.update(id, request);

        return new ApiResponseDto<>(
                "Solar record updated successfully",
                data
        );
    }


    // ================= DELETE =================
    @DeleteMapping("/{id}")
    public ApiResponseDto<String> delete(
            @PathVariable String id
    ) {

        service.delete(id);

        return new ApiResponseDto<>(
                "Solar record deleted successfully",
                null
        );
    }
}
