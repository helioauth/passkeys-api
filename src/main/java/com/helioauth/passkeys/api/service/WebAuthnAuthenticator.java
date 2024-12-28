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
import com.github.benmanes.caffeine.cache.Cache;
import com.helioauth.passkeys.api.generated.models.SignInStartResponse;
import com.helioauth.passkeys.api.generated.models.SignUpStartResponse;
import com.helioauth.passkeys.api.mapper.CredentialRegistrationResultMapper;
import com.helioauth.passkeys.api.service.dto.CredentialAssertionResult;
import com.helioauth.passkeys.api.service.dto.CredentialRegistrationResult;
import com.helioauth.passkeys.api.service.exception.CredentialAssertionFailedException;
import com.helioauth.passkeys.api.service.exception.CredentialRegistrationFailedException;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.FinishAssertionOptions;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.AuthenticatorAssertionResponse;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.ClientAssertionExtensionOutputs;
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.ResidentKeyRequirement;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.exception.AssertionFailedException;
import com.yubico.webauthn.exception.RegistrationFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.Instant;

/**
 * @author Viktor Stanchev
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebAuthnAuthenticator {

    private final RelyingParty relyingParty;

    private final CredentialRegistrationResultMapper credentialRegistrationResultMapper;

    private final Cache<String, String> webAuthnRequestCache;

    private static final SecureRandom random = new SecureRandom();

    public SignUpStartResponse startRegistration(String name) throws JsonProcessingException {
        ByteArray id = generateRandom();
        return startRegistration(name, id);
    }

    public SignUpStartResponse startRegistration(String name, ByteArray userId) throws JsonProcessingException {
        ResidentKeyRequirement residentKeyRequirement = ResidentKeyRequirement.PREFERRED;

        PublicKeyCredentialCreationOptions request = relyingParty.startRegistration(StartRegistrationOptions.builder()
            .user(
                UserIdentity.builder()
                    .name(name)
                    .displayName(name)
                    .id(userId)
                    .build()
            )
            .authenticatorSelection(
                AuthenticatorSelectionCriteria.builder()
                    .residentKey(residentKeyRequirement)
                    .build()
            )
            .build()
        );

        String requestId = generateRandom().getHex();
        webAuthnRequestCache.put(requestId, request.toJson());

        return new SignUpStartResponse(
            requestId,
            request.toCredentialsCreateJson()
        );
    }

    public String getUsernameByRequestId(String requestId) throws IOException {
        String requestJson = webAuthnRequestCache.getIfPresent(requestId);
        if (requestJson == null) {
            throw new CredentialRegistrationFailedException("Request not found.");
        }

        PublicKeyCredentialCreationOptions request = PublicKeyCredentialCreationOptions.fromJson(requestJson);
        return request.getUser().getName();
    }

    public CredentialRegistrationResult finishRegistration(String requestId, String publicKeyCredentialJson) throws IOException {
        String requestJson = webAuthnRequestCache.getIfPresent(requestId);
        if (requestJson == null) {
            throw new CredentialRegistrationFailedException("Request not found.");
        }
        webAuthnRequestCache.invalidate(requestId);

        PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> pkc =
            PublicKeyCredential.parseRegistrationResponseJson(publicKeyCredentialJson);

        try {
            PublicKeyCredentialCreationOptions request = PublicKeyCredentialCreationOptions.fromJson(requestJson);

            RegistrationResult result = relyingParty.finishRegistration(FinishRegistrationOptions.builder()
                    .request(request)
                    .response(pkc)
                    .build());

            return credentialRegistrationResultMapper.fromRegistrationResult(result, request.getUser(), pkc.getResponse());

        } catch (RegistrationFailedException e) {
            throw new CredentialRegistrationFailedException(e.getMessage(), e);
        }
    }

    public SignInStartResponse startAssertion(String name) throws JsonProcessingException {
        AssertionRequest request = relyingParty.startAssertion(StartAssertionOptions.builder()
                .username(name)
                .build());

        String requestId = generateRandom().getHex();
        webAuthnRequestCache.put(requestId, request.toJson());

        return new SignInStartResponse(requestId, request.toCredentialsGetJson());
    }

    public CredentialAssertionResult finishAssertion(String requestId, String publicKeyCredentialJson) throws IOException {
        String requestJson = webAuthnRequestCache.getIfPresent(requestId);
        if (requestJson == null) {
            log.error("Request id {} not found in cache", requestId);
            throw new CredentialAssertionFailedException();
        }
        webAuthnRequestCache.invalidate(requestId);

        try {
            PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> pkc =
                    PublicKeyCredential.parseAssertionResponseJson(publicKeyCredentialJson);

            AssertionRequest request = AssertionRequest.fromJson(requestJson);
            AssertionResult result = relyingParty.finishAssertion(FinishAssertionOptions.builder()
                    .request(request)  // The PublicKeyCredentialRequestOptions from startAssertion above
                    .response(pkc)
                    .build());

            if (result.isSuccess()) {
                log.info(result.toString());

                RegisteredCredential credential = result.getCredential();

                return new CredentialAssertionResult(
                        result.getSignatureCount(),
                        Instant.now(),
                        credential.isBackedUp().orElse(false),
                        credential.getUserHandle().getBase64(),
                        credential.getCredentialId().getBase64(),
                        result.getUsername()
                );
            }
        } catch (AssertionFailedException e) {
            log.info("Assertion failed", e);
            throw new CredentialAssertionFailedException();
        }

        throw new CredentialAssertionFailedException();
    }

    private static ByteArray generateRandom() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return new ByteArray(bytes);
    }
}