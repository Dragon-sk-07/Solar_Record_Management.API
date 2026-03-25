package com.suraj.Customer_Portal_29.controller;

import com.suraj.Customer_Portal_29.dto.request.*;
import com.suraj.Customer_Portal_29.dto.response.*;
import com.suraj.Customer_Portal_29.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
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
        return new ApiResponseDto("User Registered Successfully",null);
    }

    @PostMapping("/send-email-otp")
    public ApiResponseDto sendEmailOtp(@RequestParam String email) {
        service.sendOtp(email);
        return new ApiResponseDto("OTP Sent",null);
    }

    @PostMapping("/verify-email-otp")
    public ApiResponseDto verifyEmailOtp(
            @RequestParam String email,
            @RequestParam String otp) {
        service.verifyOtp(email, otp);
        return new ApiResponseDto("Email Verified",null);
    }

    @PostMapping("/send-mobile-otp")
    public ApiResponseDto sendMobileOtp(@RequestParam String mobile) {
        service.sendOtp(mobile);
        return new ApiResponseDto("OTP Sent",null);
    }

    @PostMapping("/verify-mobile-otp")
    public ApiResponseDto verifyMobileOtp(
            @RequestParam String mobile,
            @RequestParam String otp) {
        service.verifyOtp(mobile, otp);
        return new ApiResponseDto("Mobile Verified",null);
    }

}
