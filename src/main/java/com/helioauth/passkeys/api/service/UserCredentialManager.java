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
import com.helioauth.passkeys.api.contract.SignUpFinishRequest;
import com.helioauth.passkeys.api.contract.SignUpFinishResponse;
import com.helioauth.passkeys.api.contract.SignUpStartResponse;
import com.helioauth.passkeys.api.domain.User;
import com.helioauth.passkeys.api.domain.UserCredential;
import com.helioauth.passkeys.api.domain.UserCredentialRepository;
import com.helioauth.passkeys.api.domain.UserRepository;
import com.helioauth.passkeys.api.mapper.UserCredentialMapper;
import com.helioauth.passkeys.api.service.dto.CredentialRegistrationResultDto;
import com.helioauth.passkeys.api.service.exception.CreateCredentialFailedException;
import com.helioauth.passkeys.api.service.exception.SignUpFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author Viktor Stanchev
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserCredentialManager {
    private final WebAuthnAuthenticator webAuthnAuthenticator;
    private final UserRepository userRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final UserCredentialMapper userCredentialMapper;

    public SignUpStartResponse createCredential(String name) {
        try {
            return webAuthnAuthenticator.startRegistration(name);
        } catch (JsonProcessingException e) {
            log.error("Creating a new credential failed", e);
            throw new CreateCredentialFailedException();
        }
    }

    public SignUpFinishResponse finishCreateCredential(SignUpFinishRequest request) {
        try {
            CredentialRegistrationResultDto result = webAuthnAuthenticator.finishRegistration(
                    request.requestId(),
                    request.publicKeyCredential()
            );

            User user = userRepository.findByName(result.name()).orElseThrow(CreateCredentialFailedException::new);

            UserCredential userCredential = userCredentialMapper.fromCredentialRegistrationResult(result);
            userCredential.setUser(user);
            userCredentialRepository.save(userCredential);

            return new SignUpFinishResponse(request.requestId(), user.getId());
        } catch (IOException e) {
            log.error("Register Credential failed", e);
            throw new SignUpFailedException();
        }
    }
}
