package com.suraj.Customer_Portal_29.service;

import com.suraj.Customer_Portal_29.config.JwtTokenProvider;
import com.suraj.Customer_Portal_29.dto.request.*;
import com.suraj.Customer_Portal_29.dto.response.LoginResponseDto;
import com.suraj.Customer_Portal_29.entity.OtpVerification;
import com.suraj.Customer_Portal_29.entity.Owner;
import com.suraj.Customer_Portal_29.repository.OtpRepository;
import com.suraj.Customer_Portal_29.repository.OwnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Autowired;


import java.time.LocalDateTime;

@Service
public class AuthService {

    private final OwnerRepository repo;
    private final JwtTokenProvider jwtProvider;
    private final java.util.concurrent.ConcurrentHashMap<String, com.suraj.Customer_Portal_29.entity.Owner> userCache = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<String, Long> cacheTime = new java.util.concurrent.ConcurrentHashMap<>();
    private static final long CACHE_DURATION = 300000L;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(OwnerRepository repo, JwtTokenProvider jwtProvider) {
        this.repo = repo;
        this.jwtProvider = jwtProvider;
    }

    public LoginResponseDto login(LoginRequestDto request) {
        String email = request.getEmail();
        com.suraj.Customer_Portal_29.entity.Owner owner = userCache.get(email);
        if (owner == null || System.currentTimeMillis() - cacheTime.getOrDefault(email, 0L) > CACHE_DURATION) {
            owner = repo.findByEmail(email).orElseThrow(() -> new RuntimeException("Invalid credentials"));
            userCache.put(email, owner);
            cacheTime.put(email, System.currentTimeMillis());
        }
        if (!encoder.matches(request.getPassword(), owner.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        String token = jwtProvider.generateToken(owner.getEmail());
        return new LoginResponseDto(token, owner.getName());
    }

    public void register(RegisterRequestDto request) {

        Owner owner = new Owner();
        owner.setName(request.getName());
        owner.setEmail(request.getEmail());
        owner.setMobile(request.getMobile());
        owner.setPassword(encoder.encode(request.getPassword()));

        repo.save(owner);
    }


    @Autowired
    private OtpRepository otpRepo;

    public void sendOtp(String target) {

        // Remove previous OTP if exists
        otpRepo.findTopByTargetOrderByExpiryTimeDesc(target)
                .ifPresent(otpRepo::delete);

        // Generate 6-digit OTP
        String otp = String.valueOf((int)(Math.random() * 900000) + 100000);

        // Save OTP with 5 minutes expiry
        OtpVerification o = new OtpVerification();
        o.setTarget(target);
        o.setOtp(otp);
        o.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        otpRepo.save(o);

        if (target.contains("@")) {
            // Email OTP with user identifier
            System.out.println("=================================");
            System.out.println("Email: " + target + " | OTP: " + otp);
            System.out.println("=================================");

            // Then try sending email
            try {
                sendEmailOtp(target, otp);
            } catch (Exception e) {
                System.err.println("Email sending failed for " + target + ": " + e.getMessage());
            }
        } else {
            // Mobile OTP with user identifier
            System.out.println("=================================");
            System.out.println("Mobile: " + target + " | OTP: " + otp);
            System.out.println("=================================");
        }
    }

    public void verifyOtp(String target, String otp) {

        OtpVerification record =
                otpRepo.findTopByTargetOrderByExpiryTimeDesc(target)
                        .orElseThrow(() ->
                                new RuntimeException("OTP not found"));


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
