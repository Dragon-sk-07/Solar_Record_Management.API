package com.suraj.Customer_Portal_29.controller;

import com.suraj.Customer_Portal_29.dto.request.UrlRequestDto;
import com.suraj.Customer_Portal_29.dto.response.UrlResponseDto;
import com.suraj.Customer_Portal_29.service.UrlService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UrlController {

    private final UrlService urlService;

    @GetMapping
    public ResponseEntity<List<UrlResponseDto>> getUserUrls(Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(urlService.getUserUrls(userId));
    }

    @PostMapping
    public ResponseEntity<UrlResponseDto> addUrl(
            Authentication authentication,
            @Valid @RequestBody UrlRequestDto request) {
        String userId = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(urlService.addUrl(userId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UrlResponseDto> updateUrl(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UrlRequestDto request) {
        String userId = authentication.getName();
        return ResponseEntity.ok(urlService.updateUrl(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUrl(
            Authentication authentication,
            @PathVariable Long id) {
        String userId = authentication.getName();
        urlService.deleteUrl(userId, id);
        return ResponseEntity.noContent().build();
    }
}