package com.hung.wms.service.impl;

import com.hung.wms.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;

@Service
public class JwtServiceImpl implements JwtService {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Duration expiration;

    // Built once at startup: decoding the Base64 secret and building a parser on every request is wasted work
    private SecretKey signingKey;
    private JwtParser parser;

    @PostConstruct
    void init() {
        signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        parser = Jwts.parser().verifyWith(signingKey).build();
    }

    @Override
    public String generateToken(UserDetails userDetails) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date(now))
                .expiration(new Date(now + expiration.toMillis()))
                .signWith(signingKey)
                .compact();
    }

    @Override
    public String extractUsername(String jwt) {
        return parseClaims(jwt).getSubject();
    }

    // The filter already verified the token in extractUsername, so only the subject needs comparing here
    @Override
    public boolean isTokenValid(String jwt, UserDetails userDetails) {
        return parseClaims(jwt).getSubject().equals(userDetails.getUsername());
    }

    // Verifies signature and expiry; throws JwtException on any problem
    private Claims parseClaims(String jwt) {
        return parser.parseSignedClaims(jwt).getPayload();
    }
}
