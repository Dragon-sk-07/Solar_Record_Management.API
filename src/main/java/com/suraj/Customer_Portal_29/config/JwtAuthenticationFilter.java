package com.suraj.Customer_Portal_29.config;

import com.suraj.Customer_Portal_29.repository.OwnerRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtProvider;
    private final OwnerRepository ownerRepository;

    public JwtAuthenticationFilter(JwtTokenProvider jwtProvider,
                                   OwnerRepository ownerRepository) {
        this.jwtProvider = jwtProvider;
        this.ownerRepository = ownerRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            chain.doFilter(request, response);
            return;
        }

        String path = request.getServletPath();

        if (path.startsWith("/auth") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs")) {
            chain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (jwtProvider.validateToken(token)) {
                String email = jwtProvider.getEmailFromToken(token);
                Claims claims = jwtProvider.getAllClaimsFromToken(token);

                String role = claims.get("role", String.class);
                List<String> permissionsList = claims.get("permissions", List.class);

                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));

                if (permissionsList != null) {
                    for (String permission : permissionsList) {
                        authorities.add(new SimpleGrantedAuthority(permission));
                    }
                }

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(email, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(auth);

                try {
                    ownerRepository.findByEmailIgnoreCase(email.trim())
                            .ifPresent(CurrentUserContext::set);
                } catch (Exception ignored) {
                }
            }
        }

        try {
            chain.doFilter(request, response);
        } finally {
            CurrentUserContext.clear();
        }
    }
}