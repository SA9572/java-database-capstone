package com.project.back_end.services;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.project.back_end.repository.AdminRepository;
import com.project.back_end.repository.DoctorRepository;
import com.project.back_end.repository.PatientRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class TokenService {

    @Value("${jwt.secret}")
    private String secret;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    // Token valid for 7 days
    private static final long EXPIRATION_MS = 7L * 24 * 60 * 60 * 1000;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Generate a JWT for a given identifier (username or email)
    public String generateToken(String identifier) {
        return Jwts.builder()
                .setSubject(identifier)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Extract the identifier (subject) from a token
    public String extractIdentifier(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claimsResolver.apply(claims);
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

    // Validates token signature/expiry AND checks the identifier still exists for the given role
    public boolean validateToken(String token, String role) {
        try {
            String identifier = extractIdentifier(token);

            if (isTokenExpired(token)) {
                return false;
            }

            switch (role.toLowerCase()) {
                case "admin":
                    return adminRepository.findByUsername(identifier) != null;
                case "doctor":
                    return doctorRepository.findByEmail(identifier) != null;
                case "patient":
                    return patientRepository.findByEmail(identifier) != null;
                default:
                    return false;
            }
        } catch (Exception e) {
            // Covers malformed token, bad signature, expired, etc.
            return false;
        }
    }
}