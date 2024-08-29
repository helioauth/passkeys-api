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

package com.helioauth.passkeys.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.helioauth.passkeys.api.contract.StartAssertionResponse;
import com.helioauth.passkeys.api.domain.UserCredentialRepository;
import com.helioauth.passkeys.api.service.dto.CredentialAssertionResultDto;
import com.helioauth.passkeys.api.service.exception.SignInFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

/**
 * @author Viktor Stanchev
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserSignInService {
    private final UserCredentialRepository userCredentialRepository;

    private final WebAuthnAuthenticator webAuthnAuthenticator;

    public StartAssertionResponse startAssertion(String name) throws JsonProcessingException {
        return webAuthnAuthenticator.startAssertion(name);
    }

    @Transactional
    public String finishAssertion(String requestId, String publicKeyCredentialJson) {
        try {
            CredentialAssertionResultDto result = webAuthnAuthenticator.finishAssertion(requestId, publicKeyCredentialJson);

            userCredentialRepository.updateUsageByUserHandleAndCredentialId(
                    result.signatureCount(),
                    result.lastUsedAt(),
                    result.isBackedUp(),
                    result.userHandle(),
                    result.credentialId()
            );

            return result.username();
        } catch (IOException e) {
            log.error("Sign in failed", e);
            throw new SignInFailedException();
        }
    }
}
