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
import com.helioauth.passkeys.api.domain.User;
import com.helioauth.passkeys.api.domain.UserCredential;
import com.helioauth.passkeys.api.domain.UserCredentialRepository;
import com.helioauth.passkeys.api.domain.UserRepository;
import com.helioauth.passkeys.api.generated.models.SignUpFinishResponse;
import com.helioauth.passkeys.api.generated.models.SignUpStartResponse;
import com.helioauth.passkeys.api.mapper.RegistrationResponseMapper;
import com.helioauth.passkeys.api.mapper.UserCredentialMapper;
import com.helioauth.passkeys.api.service.dto.CredentialRegistrationResult;
import com.helioauth.passkeys.api.service.dto.RegistrationStartRequest;
import com.helioauth.passkeys.api.service.dto.UserSignupStartRequest;
import com.helioauth.passkeys.api.service.exception.SignUpFailedException;
import com.helioauth.passkeys.api.service.exception.UsernameAlreadyRegisteredException;
import com.yubico.webauthn.data.ByteArray;
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
    private final UserCredentialMapper userCredentialMapper;
    private final RegistrationResponseMapper registrationResponseMapper;

    public SignUpStartResponse startRegistration(UserSignupStartRequest request) {
        String name = request.getName();
        String rpId = request.getRpId();
        String rpName = request.getRpName();

        if (userRepository.findByName(name).isPresent()) {
            log.warn("Attempted to start registration for already existing username: {}", name);
            throw new UsernameAlreadyRegisteredException();
        }

        try {
            ByteArray userId = WebAuthnAuthenticator.generateRandom();
            return registrationResponseMapper.toSignUpStartResponse(
                webAuthnAuthenticator.startRegistration(
                    RegistrationStartRequest.builder()
                        .name(name)
                        .userId(userId)
                        .rpHostname(rpId)
                        .rpName(rpName)
                        .build()
                )
            );
        } catch (JsonProcessingException e) {
            log.error("Register Credential failed for user '{}' and rpId '{}' with name '{}'", name, rpId, rpName, e);
            throw new SignUpFailedException();
        }
    }

    @Transactional
    public SignUpFinishResponse finishRegistration(String requestId, String publicKeyCredentialJson) {
        try {
            String username = webAuthnAuthenticator.getUsernameByRequestId(requestId);
            if (userRepository.findByName(username).isPresent()) {
                throw new UsernameAlreadyRegisteredException();
            }

            CredentialRegistrationResult result = webAuthnAuthenticator.finishRegistration(requestId, publicKeyCredentialJson);

            User user = userRepository.save(User.builder()
                    .name(result.name())
                    .displayName(result.displayName())
                    .build());

            UserCredential userCredential = userCredentialMapper.fromCredentialRegistrationResult(result);
            userCredential.setUser(user);
            userCredentialRepository.save(userCredential);

            return new SignUpFinishResponse(requestId, user.getId());
        } catch (IOException e) {
            log.error("Register Credential failed", e);
            throw new SignUpFailedException();
        }
    }
}
