/*
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.helioauth.passkeys.api.auth;

import com.helioauth.passkeys.api.domain.ClientApplication;
import com.helioauth.passkeys.api.domain.ClientApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationApiKeyAuthenticationProvider implements AuthenticationProvider {

    private final ClientApplicationRepository clientApplicationRepository;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String apiKeyHeader = (String) authentication.getPrincipal();
        
        if (apiKeyHeader == null || apiKeyHeader.isBlank()) {
            throw new BadCredentialsException("Application API key header is missing or empty");
        }
        
        ClientApplication clientApp = clientApplicationRepository.findByApiKey(apiKeyHeader)
            .orElseThrow(() -> new BadCredentialsException("Invalid api key"));

        PreAuthenticatedAuthenticationToken authenticatedToken = new PreAuthenticatedAuthenticationToken(
            clientApp.getId(),
            clientApp,
            List.of(new SimpleGrantedAuthority("ROLE_APPLICATION"))
        );
        authenticatedToken.setDetails(clientApp);

        return authenticatedToken;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return PreAuthenticatedAuthenticationToken.class.isAssignableFrom(authentication);
    }
}