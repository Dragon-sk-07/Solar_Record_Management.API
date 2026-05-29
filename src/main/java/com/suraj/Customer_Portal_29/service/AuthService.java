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
import java.util.Optional;

@Service
public class AuthService {

    private final OwnerRepository repo;
    private final JwtTokenProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final OtpRepository otpRepo;
    private final JavaMailSender mailSender;

    public AuthService(OwnerRepository repo,
                       JwtTokenProvider jwtProvider,
                       PasswordEncoder passwordEncoder,
                       OtpRepository otpRepo,
                       JavaMailSender mailSender) {
        this.repo = repo;
        this.jwtProvider = jwtProvider;
        this.passwordEncoder = passwordEncoder;
        this.otpRepo = otpRepo;
        this.mailSender = mailSender;
    }

    public LoginResponseDto login(LoginRequestDto request) {
        System.out.println("========== [DEBUG] LOGIN ATTEMPT START ==========");
        System.out.println("[DEBUG] Login Email: " + request.getEmail());
        System.out.println("[DEBUG] Login Password: " + request.getPassword());

        String email = request.getEmail();

        Optional<Owner> ownerOptional = repo.findByEmail(email);
        System.out.println("[DEBUG] User exists in database: " + ownerOptional.isPresent());

        if (ownerOptional.isEmpty()) {
            System.out.println("[DEBUG] User NOT FOUND - throwing Invalid credentials");
            System.out.println("========== [DEBUG] LOGIN ATTEMPT END ==========");
            throw new RuntimeException("Invalid credentials");
        }

        Owner owner = ownerOptional.get();
        System.out.println("[DEBUG] User found - ID: " + owner.getId());
        System.out.println("[DEBUG] User Name: " + owner.getName());
        System.out.println("[DEBUG] User Email: " + owner.getEmail());
        System.out.println("[DEBUG] User isActive status: " + owner.isActive());
        System.out.println("[DEBUG] User Role: " + owner.getRole());

        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), owner.getPassword());
        System.out.println("[DEBUG] Password matches: " + passwordMatches);

        if (!passwordMatches) {
            System.out.println("[DEBUG] Password MISMATCH - throwing Invalid credentials");
            System.out.println("========== [DEBUG] LOGIN ATTEMPT END ==========");
            throw new RuntimeException("Invalid credentials");
        }

        if (!owner.isActive()) {
            System.out.println("[DEBUG] User is DEACTIVATED - throwing specific message");
            System.out.println("[DEBUG] Deactivation message: Your account has been deactivated. Please contact super admin.");
            System.out.println("========== [DEBUG] LOGIN ATTEMPT END ==========");
            throw new RuntimeException("Your account has been deactivated. Please contact super admin.");
        }

        System.out.println("[DEBUG] User is ACTIVE - generating token");
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", owner.getRole().name());
        claims.put("permissions", owner.getPermissions());
        String token = jwtProvider.generateToken(owner.getEmail(), claims);
        System.out.println("[DEBUG] Token generated successfully");
        System.out.println("========== [DEBUG] LOGIN ATTEMPT END ==========");

        return new LoginResponseDto(token, owner.getName(), owner.getRole().name(), owner.getPermissions());
    }

    public void register(RegisterRequestDto request) {
        System.out.println("========== [DEBUG] REGISTER ATTEMPT ==========");
        System.out.println("[DEBUG] Register Email: " + request.getEmail());
        System.out.println("[DEBUG] Register Name: " + request.getName());
        System.out.println("[DEBUG] Register Mobile: " + request.getMobile());

        Owner owner = new Owner();
        owner.setName(request.getName());
        owner.setEmail(request.getEmail());
        owner.setMobile(request.getMobile());
        owner.setPassword(passwordEncoder.encode(request.getPassword()));
        owner.setActive(true);

        Owner savedOwner = repo.save(owner);
        System.out.println("[DEBUG] User registered successfully with ID: " + savedOwner.getId());
        System.out.println("[DEBUG] User active status set to: " + savedOwner.isActive());
        System.out.println("========== [DEBUG] REGISTER END ==========");
    }

    @Autowired
    private void setOtpRepoAndMailSender(OtpRepository otpRepo, JavaMailSender mailSender) {
        // Constructor injection already handles these
    }

    public void sendOtp(String target) {
        System.out.println("========== [DEBUG] SEND OTP ==========");
        System.out.println("[DEBUG] Target: " + target);

        otpRepo.findTopByTargetOrderByExpiryTimeDesc(target)
                .ifPresent(otp -> {
                    System.out.println("[DEBUG] Removing existing OTP for target: " + target);
                    otpRepo.delete(otp);
                });

        String otp = String.valueOf((int)(Math.random() * 900000) + 100000);
        System.out.println("[DEBUG] Generated OTP: " + otp);

        OtpVerification o = new OtpVerification();
        o.setTarget(target);
        o.setOtp(otp);
        o.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        otpRepo.save(o);
        System.out.println("[DEBUG] OTP saved with expiry: " + o.getExpiryTime());

        if (target.contains("@")) {
            System.out.println("[DEBUG] Target is EMAIL - sending OTP via email");
            System.out.println("=================================");
            System.out.println("Email: " + target + " | OTP: " + otp);
            System.out.println("=================================");
            try {
                sendEmailOtp(target, otp);
                System.out.println("[DEBUG] Email sent successfully to: " + target);
            } catch (Exception e) {
                System.err.println("[DEBUG] Email sending failed: " + e.getMessage());
            }
        } else {
            System.out.println("[DEBUG] Target is MOBILE - OTP: " + otp);
            System.out.println("=================================");
            System.out.println("Mobile: " + target + " | OTP: " + otp);
            System.out.println("=================================");
        }
        System.out.println("========== [DEBUG] SEND OTP END ==========");
    }

    public void verifyOtp(String target, String otp) {
        System.out.println("========== [DEBUG] VERIFY OTP ==========");
        System.out.println("[DEBUG] Target: " + target);
        System.out.println("[DEBUG] Received OTP: " + otp);

        OtpVerification record = otpRepo.findTopByTargetOrderByExpiryTimeDesc(target)
                .orElseThrow(() -> {
                    System.out.println("[DEBUG] OTP record NOT FOUND for target: " + target);
                    return new RuntimeException("OTP not found");
                });

        System.out.println("[DEBUG] Found OTP record - Stored OTP: " + record.getOtp());
        System.out.println("[DEBUG] Expiry Time: " + record.getExpiryTime());
        System.out.println("[DEBUG] Current Time: " + LocalDateTime.now());

        if (record.getExpiryTime().isBefore(LocalDateTime.now())) {
            System.out.println("[DEBUG] OTP has EXPIRED");
            throw new RuntimeException("OTP Expired");
        }

        if (!record.getOtp().equals(otp.trim())) {
            System.out.println("[DEBUG] OTP MISMATCH - Expected: " + record.getOtp() + ", Received: " + otp.trim());
            throw new RuntimeException("Invalid OTP");
        }

        System.out.println("[DEBUG] OTP VERIFIED successfully");
        otpRepo.delete(record);
        System.out.println("[DEBUG] OTP record deleted");
        System.out.println("========== [DEBUG] VERIFY OTP END ==========");
    }

    private void sendEmailOtp(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("OTP Verification");
        message.setText("Your OTP is: " + otp);
        mailSender.send(message);
    }
}