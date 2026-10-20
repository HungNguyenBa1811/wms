package com.hung.wms.service.impl;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtServiceImpl")
class JwtServiceImplTest {
    // Same dummy key as application-test.yaml, never use it outside tests
    private static final String SECRET =
            "dGVzdC1vbmx5LWp3dC1zZWNyZXQtZm9yLXdtcy11bml0LXRlc3RzLW5vdC1mb3ItcHJvZHVjdGlvbi11c2UhIQ==";

    private JwtServiceImpl jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", Duration.ofHours(1));
        jwtService.init();
    }

    @Test
    @DisplayName("token chứa username (subject) và role (không có tiền tố ROLE_)")
    void tokenCarriesUsernameAndRole() {
        // given
        UserDetails admin = User.withUsername("admin").password("x").roles("ADMIN").build();

        // when
        String token = jwtService.generateToken(admin);

        // then
        assertThat(jwtService.extractUsername(token)).isEqualTo("admin");
        assertThat(jwtService.extractRole(token)).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("token cũ không có claim role -> extractRole trả null")
    void tokenWithoutRoleClaim() {
        // given: token signed with the same key but issued before the role claim existed
        String oldToken = Jwts.builder()
                .subject("staff")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)))
                .compact();

        // when / then
        assertThat(jwtService.extractUsername(oldToken)).isEqualTo("staff");
        assertThat(jwtService.extractRole(oldToken)).isNull();
    }
}
