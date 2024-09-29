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

package com.helioauth.passkeys.api.auth;

import com.helioauth.passkeys.api.config.properties.AdminConfigProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Viktor Stanchev
 */
@Service
@RequiredArgsConstructor
public class RequestHeaderAuthenticationProvider implements AuthenticationProvider {

    private final AdminConfigProperties adminConfigProperties;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String authHeaderValue = String.valueOf(authentication.getPrincipal());

        String apiKey = adminConfigProperties.getAuth().getApiKey();
        if(authHeaderValue == null || authHeaderValue.isBlank() || !authHeaderValue.equals(apiKey)) {
            throw new BadCredentialsException("Bad admin credentials");
        }

        return new PreAuthenticatedAuthenticationToken(
            authentication.getPrincipal(),
            null,
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(PreAuthenticatedAuthenticationToken.class);
    }
}
