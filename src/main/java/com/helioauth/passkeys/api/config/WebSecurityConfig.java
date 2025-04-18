/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.helioauth.passkeys.api.config;

import com.helioauth.passkeys.api.auth.AdminApiAuthenticationProvider;
import com.helioauth.passkeys.api.auth.ApplicationApiKeyAuthenticationProvider;
import com.helioauth.passkeys.api.auth.ApplicationIdAuthenticationProvider;
import com.helioauth.passkeys.api.config.properties.AdminConfigProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;
import org.springframework.security.web.header.HeaderWriterFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.helioauth.passkeys.api.domain.ClientApplication;


/**
 * @author Viktor Stanchev
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {
    private final AdminApiAuthenticationProvider adminApiAuthenticationProvider;
    private final ApplicationIdAuthenticationProvider applicationIdAuthenticationProvider;
    private final ApplicationApiKeyAuthenticationProvider applicationApiKeyAuthenticationProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                          RequestHeaderAuthenticationFilter adminAuthFilter,
                                          RequestHeaderAuthenticationFilter applicationIdAuthFilter,
                                          RequestHeaderAuthenticationFilter applicationApiKeyAuthFilter) throws Exception {

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterAfter(adminAuthFilter, HeaderWriterFilter.class)
            .addFilterAfter(applicationIdAuthFilter, HeaderWriterFilter.class)
            .addFilterAfter(applicationApiKeyAuthFilter, HeaderWriterFilter.class)
            .authorizeHttpRequests(registry -> registry
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/v1/signup/start").hasRole("FRONTEND_APPLICATION")
                .requestMatchers("/v1/signup/finish").hasRole("APPLICATION")
                .anyRequest().permitAll()
            )
            .exceptionHandling(config -> config
                .authenticationEntryPoint((_, response, _) ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED)));

        return http.build();
    }

    @Bean
    public RequestHeaderAuthenticationFilter adminAuthFilter(AdminConfigProperties adminConfigProperties) {
        RequestHeaderAuthenticationFilter filter = new RequestHeaderAuthenticationFilter();
        filter.setPrincipalRequestHeader(adminConfigProperties.getAuth().getHeaderName());
        filter.setExceptionIfHeaderMissing(false);
        filter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/admin/**"));
        filter.setAuthenticationManager(adminAuthenticationManager());

        return filter;
    }

    @Bean
    public RequestHeaderAuthenticationFilter applicationIdAuthFilter(@Value("${app.auth.app-id-header}") String authHeader) {
        RequestHeaderAuthenticationFilter filter = new RequestHeaderAuthenticationFilter();
        filter.setPrincipalRequestHeader(authHeader);
        filter.setExceptionIfHeaderMissing(false);
        filter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/v1/signup/start"));
        filter.setAuthenticationManager(appIdAuthenticationManager());

        return filter;
    }

    @Bean
    public RequestHeaderAuthenticationFilter applicationApiKeyAuthFilter(@Value("${app.auth.api-key-header}") String authHeader) {
        RequestHeaderAuthenticationFilter filter = new RequestHeaderAuthenticationFilter();
        filter.setPrincipalRequestHeader(authHeader);
        filter.setExceptionIfHeaderMissing(false);
        filter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/v1/signup/finish"));
        filter.setAuthenticationManager(appApiKeyAuthenticationManager());

        return filter;
    }

    protected AuthenticationManager adminAuthenticationManager() {
        return new ProviderManager(List.of(adminApiAuthenticationProvider));
    }

    protected AuthenticationManager appIdAuthenticationManager() {
        return new ProviderManager(List.of(applicationIdAuthenticationProvider));
    }

    protected AuthenticationManager appApiKeyAuthenticationManager() {
        return new ProviderManager(List.of(applicationApiKeyAuthenticationProvider));
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        return _ -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof ClientApplication)) {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.applyPermitDefaultValues();
                return configuration;
            }

            ClientApplication clientApplication = (ClientApplication) authentication.getPrincipal();
            String allowedOrigins = clientApplication.getAllowedOrigins();

            CorsConfiguration configuration = new CorsConfiguration();
            if (allowedOrigins != null && !allowedOrigins.isEmpty()) {
                configuration.setAllowedOrigins(List.of(allowedOrigins.split(",")));
                configuration.setAllowedMethods(List.of("*"));
                configuration.setAllowedHeaders(List.of("*"));
                configuration.setAllowCredentials(true);
            } else {
                configuration.applyPermitDefaultValues();
            }

            return configuration;
        };
    }
}
