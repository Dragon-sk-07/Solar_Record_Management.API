package com.suraj.Customer_Portal_29.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import com.suraj.Customer_Portal_29.entity.Owner;
import com.suraj.Customer_Portal_29.repository.OwnerRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private long expiration;
    private final OwnerRepository ownerRepository;

    public JwtTokenProvider(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String email, Map<String, Object> extraClaims) {
        Owner user = ownerRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Map<String, Object> claims = new HashMap<>(extraClaims);
        claims.put("sub", email);
        claims.put("email", email);
        claims.put("role", user.getRole().name());
        claims.put("permissions", user.getPermissions().stream().map(Enum::name).collect(Collectors.toList()));
        claims.put("vendorName", user.getName());
        claims.put("vendorAddress", user.getVendorAddress());
        claims.put("vendorMobile", user.getVendorMobile());
        claims.put("vendorEmail", user.getVendorEmail());
        claims.put("authorizedPersonName", user.getAuthorizedPersonName());
        claims.put("witness1Name", user.getWitness1Name());
        claims.put("witness1Address", user.getWitness1Address());
        claims.put("witness2Name", user.getWitness2Name());
        claims.put("witness2Address", user.getWitness2Address());
        claims.put("bankAccountName", user.getBankAccountName());
        claims.put("bankAccountNumber", user.getBankAccountNumber());
        claims.put("bankName", user.getBankName());
        claims.put("bankIfscCode", user.getBankIfscCode());
        claims.put("headerLogoUrl", user.getHeaderLogoUrl());
        claims.put("vendorSignatureUrl", user.getVendorSignatureUrl());
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        return Jwts.builder().setClaims(claims).setSubject(email).setIssuedAt(now).setExpiration(expiryDate).signWith(getSigningKey()).compact();
    }

    public String getEmailFromToken(String token) {
        return getAllClaimsFromToken(token).getSubject();
    }

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}