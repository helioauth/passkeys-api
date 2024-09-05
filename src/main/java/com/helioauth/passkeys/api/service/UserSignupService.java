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
import com.helioauth.passkeys.api.contract.SignUpStartResponse;
import com.helioauth.passkeys.api.domain.User;
import com.helioauth.passkeys.api.domain.UserCredential;
import com.helioauth.passkeys.api.domain.UserCredentialRepository;
import com.helioauth.passkeys.api.domain.UserRepository;
import com.helioauth.passkeys.api.mapper.UserCredentialMapper;
import com.helioauth.passkeys.api.service.dto.CredentialRegistrationResultDto;
import com.helioauth.passkeys.api.service.exception.SignUpFailedException;
import com.helioauth.passkeys.api.service.exception.UsernameAlreadyRegisteredException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

/**
 * @author Viktor Stanchev
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserSignupService {

    private final UserRepository userRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final WebAuthnAuthenticator webAuthnAuthenticator;
    private final UserCredentialMapper usercredentialMapper;

    public SignUpStartResponse startRegistration(String name) {
        userRepository.findByName(name).ifPresent(_ -> {
            throw new UsernameAlreadyRegisteredException();
        });

        try {
            return webAuthnAuthenticator.startRegistration(name);
        } catch (JsonProcessingException e) {
            log.error("Register Credential failed", e);
            throw new SignUpFailedException();
        }
    }

    @Transactional
    public void finishRegistration(String requestId, String publicKeyCredentialJson) {
        try {
            CredentialRegistrationResultDto result = webAuthnAuthenticator.finishRegistration(requestId, publicKeyCredentialJson);

            User user = User.builder()
                    .name(result.name())
                    .displayName(result.displayName())
                    .build();
            userRepository.save(user);

            UserCredential userCredential = usercredentialMapper.fromCredentialRegistrationResult(result);
            userCredential.setUser(user);
            userCredentialRepository.save(userCredential);
        } catch (IOException e) {
            log.error("Register Credential failed", e);
            throw new SignUpFailedException();
        }
    }
}
