package com.suraj.Customer_Portal_29.service;

import com.suraj.Customer_Portal_29.config.JwtTokenProvider;
import com.suraj.Customer_Portal_29.dto.request.*;
import com.suraj.Customer_Portal_29.dto.response.LoginResponseDto;
import com.suraj.Customer_Portal_29.entity.Owner;
import com.suraj.Customer_Portal_29.repository.OwnerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
        Owner owner = repo.findByUsernameIgnoreCase(request.getUsername()).orElse(null);
        if (owner == null || !passwordEncoder.matches(request.getPassword(), owner.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        if (!owner.isActive()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Your account has been deactivated. Please contact super admin.");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", owner.getRole().name());
        claims.put("permissions", owner.getPermissions());

        String token = jwtProvider.generateToken(owner.getUsername(), claims);

        return new LoginResponseDto(token, owner.getName(), owner.getRole().name(), owner.getPermissions());
    }

}