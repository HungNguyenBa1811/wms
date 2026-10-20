package com.hung.wms.config;

import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Source of createdBy / updatedBy for JPA auditing: the username of the logged-in user.
 * JwtAuthenticationFilter puts the username as principal, so getName() is the username (no query on users).
 * No logged-in user (register, data.sql, tests calling services directly) -> empty -> the column stays null.
 * Null instead of a fake "system" user: it cannot collide with a real username and clearly means "nobody logged in".
 */
@Component
public class SecurityAuditorAware implements AuditorAware<String> {
    @Override
    @NullMarked
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken)
            return Optional.empty();
        return Optional.ofNullable(authentication.getName());
    }
}
