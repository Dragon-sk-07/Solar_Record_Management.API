package com.suraj.Customer_Portal_29.controller;

import com.suraj.Customer_Portal_29.dto.request.*;
import com.suraj.Customer_Portal_29.dto.response.*;
import com.suraj.Customer_Portal_29.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(service.login(request));
    }

    @PostMapping("/register")
    public ApiResponseDto register(@RequestBody RegisterRequestDto request) {
        service.register(request);
        return new ApiResponseDto("User Registered Successfully", null);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleAuthException(ResponseStatusException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getReason());
        return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
    }
}