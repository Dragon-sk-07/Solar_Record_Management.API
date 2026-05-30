package com.suraj.Customer_Portal_29.service;

import com.suraj.Customer_Portal_29.config.JwtTokenProvider;
import com.suraj.Customer_Portal_29.dto.request.*;
import com.suraj.Customer_Portal_29.dto.response.LoginResponseDto;
import com.suraj.Customer_Portal_29.entity.OtpVerification;
import com.suraj.Customer_Portal_29.entity.Owner;
import com.suraj.Customer_Portal_29.repository.OtpRepository;
import com.suraj.Customer_Portal_29.repository.OwnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final OwnerRepository repo;
    private final JwtTokenProvider jwtProvider;
    private final java.util.concurrent.ConcurrentHashMap<String, Owner> userCache = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<String, Long> cacheTime = new java.util.concurrent.ConcurrentHashMap<>();
    private static final long CACHE_DURATION = 300000L;
    private final PasswordEncoder passwordEncoder;

    public AuthService(OwnerRepository repo, JwtTokenProvider jwtProvider, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.jwtProvider = jwtProvider;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponseDto login(LoginRequestDto request) {
        System.out.println("[DEBUG] ========== LOGIN ATTEMPT ==========");
        System.out.println("[DEBUG] Login Email: " + request.getEmail());
        System.out.println("[DEBUG] Login Password length: " + (request.getPassword() != null ? request.getPassword().length() : 0));

        Owner owner = repo.findByEmail(request.getEmail()).orElse(null);

        if (owner == null) {
            System.out.println("[DEBUG] User NOT found with email: " + request.getEmail());
            throw new RuntimeException("Invalid credentials");
        }

        System.out.println("[DEBUG] Found User - ID: " + owner.getId());
        System.out.println("[DEBUG] Found User - Email: " + owner.getEmail());
        System.out.println("[DEBUG] Found User - Name: " + owner.getName());
        System.out.println("[DEBUG] Found User - Active: " + owner.isActive());

        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), owner.getPassword());
        System.out.println("[DEBUG] Password matches: " + passwordMatches);

        if (!passwordMatches) {
            System.out.println("[DEBUG] Password mismatch for email: " + request.getEmail());
            throw new RuntimeException("Invalid credentials");
        }
        if (!owner.isActive()) {
            System.out.println("[DEBUG] Account deactivated for: " + request.getEmail());
            throw new RuntimeException("Your account has been deactivated. Please contact super admin.");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", owner.getRole().name());
        claims.put("permissions", owner.getPermissions());
        String token = jwtProvider.generateToken(owner.getEmail(), claims);

        System.out.println("[DEBUG] Login successful - Token generated for: " + owner.getEmail());
        System.out.println("[DEBUG] ========== LOGIN SUCCESS ==========");

        return new LoginResponseDto(token, owner.getName(), owner.getRole().name(), owner.getPermissions());
    }

    public void register(RegisterRequestDto request) {
        Owner owner = new Owner();
        owner.setName(request.getName());
        owner.setEmail(request.getEmail());
        owner.setMobile(request.getMobile());
        owner.setPassword(passwordEncoder.encode(request.getPassword()));
        repo.save(owner);
    }

    @Autowired
    private OtpRepository otpRepo;

    public void sendOtp(String target) {
        otpRepo.findTopByTargetOrderByExpiryTimeDesc(target)
                .ifPresent(otpRepo::delete);

        String otp = String.valueOf((int)(Math.random() * 900000) + 100000);

        OtpVerification o = new OtpVerification();
        o.setTarget(target);
        o.setOtp(otp);
        o.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        otpRepo.save(o);

        if (target.contains("@")) {
            System.out.println("=================================");
            System.out.println("Email: " + target + " | OTP: " + otp);
            System.out.println("=================================");
            try {
                sendEmailOtp(target, otp);
            } catch (Exception e) {
                System.err.println("Email sending failed for " + target + ": " + e.getMessage());
            }
        } else {
            System.out.println("=================================");
            System.out.println("Mobile: " + target + " | OTP: " + otp);
            System.out.println("=================================");
        }
    }

    public void verifyOtp(String target, String otp) {
        OtpVerification record =
                otpRepo.findTopByTargetOrderByExpiryTimeDesc(target)
                        .orElseThrow(() -> new RuntimeException("OTP not found"));

        if (record.getExpiryTime().isBefore(LocalDateTime.now()))
            throw new RuntimeException("OTP Expired");

        if (!record.getOtp().equals(otp.trim()))
            throw new RuntimeException("Invalid OTP");

        otpRepo.delete(record);
    }

    @Autowired
    private JavaMailSender mailSender;

    private void sendEmailOtp(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("OTP Verification");
        message.setText("Your OTP is: " + otp);
        mailSender.send(message);
    }
}