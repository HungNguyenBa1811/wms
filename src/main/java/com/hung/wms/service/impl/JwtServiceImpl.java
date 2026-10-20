package com.hung.wms.service.impl;

import com.hung.wms.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;

@Service
public class JwtServiceImpl implements JwtService {
    // Role is stored in the token so JwtAuthenticationFilter does not query users on every request.
    // Trade-off: a role change in the DB only applies after the user logs in again (token expiry).
    private static final String ROLE_CLAIM = "role";
    private static final String ROLE_PREFIX = "ROLE_";

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
                .claim(ROLE_CLAIM, extractRole(userDetails))
                .issuedAt(new Date(now))
                .expiration(new Date(now + expiration.toMillis()))
                .signWith(signingKey)
                .compact();
    }

    @Override
    public String extractUsername(String jwt) {
        return parseClaims(jwt).getSubject();
    }

    // Role name without the ROLE_ prefix (ADMIN / STAFF), null for a token issued before the claim existed
    @Override
    public String extractRole(String jwt) {
        return parseClaims(jwt).get(ROLE_CLAIM, String.class);
    }

    // UserDetailsServiceImpl builds the user with .roles(role), so the only authority is ROLE_<role>
    private String extractRole(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith(ROLE_PREFIX))
                .map(authority -> authority.substring(ROLE_PREFIX.length()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("User has no role: " + userDetails.getUsername()));
    }

    // Verifies signature and expiry; throws JwtException on any problem
    private Claims parseClaims(String jwt) {
        return parser.parseSignedClaims(jwt).getPayload();
    }
}
