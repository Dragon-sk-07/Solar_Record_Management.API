package com.suraj.Customer_Portal_29.controller;

import com.suraj.Customer_Portal_29.dto.request.URL_View_Request_Dto;
import com.suraj.Customer_Portal_29.dto.response.ApiResponseDto;
import com.suraj.Customer_Portal_29.dto.response.URL_View_Response_Dto;
import com.suraj.Customer_Portal_29.service.URL_View_Service;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/url-view")
public class URL_View_Controller {

    private final URL_View_Service service;

    public URL_View_Controller(URL_View_Service service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponseDto<List<URL_View_Response_Dto>> getAll() {
        return new ApiResponseDto<>("URLs fetched successfully", service.findAllForCurrentUser());
    }

    @PostMapping
    public ResponseEntity<ApiResponseDto<URL_View_Response_Dto>> create(
            @Valid @RequestBody URL_View_Request_Dto request) {
        URL_View_Response_Dto data = service.create(request);
        return ResponseEntity.status(201)
                .body(new ApiResponseDto<>("URL added successfully", data));
    }

    @PutMapping("/{id}")
    public ApiResponseDto<URL_View_Response_Dto> update(
            @PathVariable Long id,
            @Valid @RequestBody URL_View_Request_Dto request) {
        URL_View_Response_Dto data = service.update(id, request);
        return new ApiResponseDto<>("URL updated successfully", data);
    }

    @DeleteMapping("/{id}")
    public ApiResponseDto<String> delete(@PathVariable Long id) {
        service.delete(id);
        return new ApiResponseDto<>("URL deleted successfully", null);
    }
}