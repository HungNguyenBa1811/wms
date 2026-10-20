package com.hung.wms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// Separate class instead of on WmsApplication: @WebMvcTest slices skip this @Configuration,
// otherwise they would fail with "JPA metamodel must not be empty"
@Configuration
@EnableJpaAuditing(auditorAwareRef = "securityAuditorAware")
public class JpaAuditingConfig {
}
