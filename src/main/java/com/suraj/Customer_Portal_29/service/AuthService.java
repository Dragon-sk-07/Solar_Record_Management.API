package com.suraj.Customer_Portal_29.service;

import com.suraj.Customer_Portal_29.config.JwtTokenProvider;
import com.suraj.Customer_Portal_29.dto.request.*;
import com.suraj.Customer_Portal_29.dto.response.LoginResponseDto;
import com.suraj.Customer_Portal_29.entity.Owner;
import com.suraj.Customer_Portal_29.repository.OwnerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final OwnerRepository repo;
    private final JwtTokenProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthService(OwnerRepository repo, JwtTokenProvider jwtProvider, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.jwtProvider = jwtProvider;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponseDto login(LoginRequestDto request) {
        Owner owner = repo.findByEmail(request.getEmail()).orElse(null);

        if (owner == null) {
            throw new RuntimeException("Invalid credentials");
        }

        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), owner.getPassword());

        if (!passwordMatches) {
            throw new RuntimeException("Invalid credentials");
        }
        if (!owner.isActive()) {
            throw new RuntimeException("Your account has been deactivated. Please contact super admin.");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", owner.getRole().name());
        claims.put("permissions", owner.getPermissions());
        String token = jwtProvider.generateToken(owner.getEmail(), claims);

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
}