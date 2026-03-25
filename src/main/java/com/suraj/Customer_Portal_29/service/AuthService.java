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
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(OwnerRepository repo, JwtTokenProvider jwtProvider) {
        this.repo = repo;
        this.jwtProvider = jwtProvider;
    }

    public LoginResponseDto login(LoginRequestDto request) {
        Owner Owner = repo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!encoder.matches(request.getPassword(), Owner.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

       String token = jwtProvider.generateToken(Owner.getEmail());

        return new LoginResponseDto(token,Owner.getName());
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
            // Immediately show OTP in console
            System.out.println("=================================");
            System.out.println("EMAIL OTP : " + otp);
            System.out.println("=================================");

            // Then try sending email
            try {
                sendEmailOtp(target, otp);
            } catch (Exception e) {
                System.err.println("Email sending failed: " + e.getMessage());
            }
        } else {
            // MOBILE OTP (Console)
            System.out.println("=================================");
            System.out.println("MOBILE OTP : " + otp);
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
