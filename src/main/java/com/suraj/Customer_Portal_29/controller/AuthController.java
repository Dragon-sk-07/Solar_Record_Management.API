package com.suraj.Customer_Portal_29.controller;

import com.suraj.Customer_Portal_29.dto.request.*;
import com.suraj.Customer_Portal_29.dto.response.*;
import com.suraj.Customer_Portal_29.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public LoginResponseDto login(@RequestBody LoginRequestDto request) {
        return service.login(request);
    }

    @PostMapping("/register")
    public ApiResponseDto register(@RequestBody RegisterRequestDto request) {
        service.register(request);
        return new ApiResponseDto("User Registered Successfully", null);
    }
}