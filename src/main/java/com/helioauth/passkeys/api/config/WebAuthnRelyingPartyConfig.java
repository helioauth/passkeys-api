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

import com.helioauth.passkeys.api.webauthn.DatabaseCredentialRepository;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Viktor Stanchev
 */
@Configuration
public class WebAuthnRelyingPartyConfig {

    @Bean
    public RelyingParty defaultRelyingParty(
        DatabaseCredentialRepository databaseCredentialRepository,
        WebAuthnRelyingPartyProperties properties
    ) {
        RelyingPartyIdentity rpId = RelyingPartyIdentity.builder()
                .id(properties.hostname())
                .name(properties.displayName())
                .build();

        return RelyingParty.builder()
                .identity(rpId)
                .credentialRepository(databaseCredentialRepository)
                .allowOriginPort(properties.allowOriginPort())
                .build();
    }
}
