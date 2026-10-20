package com.hung.wms.api;

import com.hung.wms.config.SecurityErrorHandler;
import com.hung.wms.config.WebSecurityConfig;
import com.hung.wms.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base for the @WebMvcTest slices. Uses the real security chain (WebSecurityConfig + SecurityErrorHandler),
 * so role rules and the 401 / 403 ErrorResponse are tested as in the app.
 * JwtAuthenticationFilter is picked up by the slice (it is a Filter) and its collaborators are mocked:
 * tests authenticate with @WithMockUser instead of a real token.
 */
@Import({WebSecurityConfig.class, SecurityErrorHandler.class})
abstract class ApiSliceTestSupport {
    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected JwtService jwtService;

    @MockitoBean
    protected UserDetailsService userDetailsService;

    /** Status + every field of ErrorResponse (timestamp, status, error, message, path, detail). */
    protected static ResultMatcher errorResponse(int status, String error, String message, String path) {
        return result -> {
            status().is(status).match(result);
            jsonPath("$.timestamp").exists().match(result);
            jsonPath("$.status").value(status).match(result);
            jsonPath("$.error").value(error).match(result);
            jsonPath("$.message").value(message).match(result);
            jsonPath("$.path").value(path).match(result);
            jsonPath("$.detail").isArray().match(result);
        };
    }
}
