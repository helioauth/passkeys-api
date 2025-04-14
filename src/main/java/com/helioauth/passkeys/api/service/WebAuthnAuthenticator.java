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
import com.helioauth.passkeys.api.config.properties.WebAuthnRelyingPartyProperties; // Added import
import com.helioauth.passkeys.api.mapper.CredentialRegistrationResultMapper;
import com.helioauth.passkeys.api.mapper.RegistrationResponseMapper;
import com.helioauth.passkeys.api.service.dto.AssertionStartResult;
import com.helioauth.passkeys.api.service.dto.CredentialAssertionResult;
import com.helioauth.passkeys.api.service.dto.CredentialRegistrationResult;
import com.helioauth.passkeys.api.service.exception.CredentialAssertionFailedException;
import com.helioauth.passkeys.api.service.exception.CredentialRegistrationFailedException;
import com.helioauth.passkeys.api.webauthn.DatabaseCredentialRepository; // Added import
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
import com.yubico.webauthn.data.RelyingPartyIdentity; // Added import
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

    // Removed: private final RelyingParty relyingParty;

    // Added dependencies needed to build RelyingParty instances
    private final DatabaseCredentialRepository databaseCredentialRepository;
    private final WebAuthnRelyingPartyProperties relyingPartyProperties;

    private final CredentialRegistrationResultMapper credentialRegistrationResultMapper;
    private final Cache<String, String> webAuthnRequestCache;

    private static final SecureRandom random = new SecureRandom();

    private final RegistrationResponseMapper registrationResponseMapper;

    public AssertionStartResult startRegistration(String name) throws JsonProcessingException {
        ByteArray id = generateRandom();
        // This existing method now implicitly calls the new 3-arg method via the 2-arg one below
        return startRegistration(name, id);
    }

    public AssertionStartResult startRegistration(String name, ByteArray userId) throws JsonProcessingException {
        // Delegate to the new 3-argument method, using the configured default RP ID from properties
        return startRegistration(name, userId, relyingPartyProperties.getHostname());
    }

    /**
     * Start registration for a given user and RP Hostname.
     * Note: Currently, the rpHostname parameter is not used to dynamically change the RelyingParty,
     * as the relyingParty bean is configured globally. This parameter is added for potential future use
     * (e.g., associating the request with a specific RP context).
     *
     * @param name       The username.
     * @param userId     The user's unique ID.
     * @param rpHostname The hostname of the relying party (currently informational).
     * @return AssertionStartResult containing the request ID and creation options.
     * @throws JsonProcessingException If JSON processing fails.
     */
    public AssertionStartResult startRegistration(String name, ByteArray userId, String rpHostname) throws JsonProcessingException {
        log.debug("Starting registration for user '{}' with id '{}' for RP '{}'", name, userId.getBase64Url(), rpHostname);

        // Build RelyingParty dynamically for this specific rpHostname
        RelyingParty relyingParty = buildRelyingParty(rpHostname);

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
        // Cache the request options JSON which includes the rpId used
        webAuthnRequestCache.put(requestId, request.toJson());

        return new AssertionStartResult(requestId, request.toCredentialsCreateJson());
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

            // Extract the rpId used during startRegistration from the cached request
            String rpId = request.getRp().getId();
            log.debug("Finishing registration for request ID '{}' using RP ID '{}'", requestId, rpId);

            // Build a RelyingParty instance matching the one used at the start
            RelyingParty relyingParty = buildRelyingParty(rpId);

            RegistrationResult result = relyingParty.finishRegistration(FinishRegistrationOptions.builder()
                    .request(request)
                    .response(pkc)
                    .build());

            return credentialRegistrationResultMapper.fromRegistrationResult(result, request.getUser(), pkc.getResponse());

        } catch (RegistrationFailedException e) {
            throw new CredentialRegistrationFailedException(e.getMessage(), e);
        }
    }

    public AssertionStartResult startAssertion(String name) throws JsonProcessingException {
        // For assertion, typically use the default configured Relying Party
        RelyingParty relyingParty = buildDefaultRelyingParty();
        log.debug("Starting assertion for user '{}' using default RP ID '{}'", name, relyingParty.getIdentity().getId());

        AssertionRequest request = relyingParty.startAssertion(StartAssertionOptions.builder()
                .username(name)
                .build());

        String requestId = generateRandom().getHex();
        webAuthnRequestCache.put(requestId, request.toJson());

        return new AssertionStartResult(requestId, request.toCredentialsGetJson());
    }

    public CredentialAssertionResult finishAssertion(String requestId, String publicKeyCredentialJson) throws IOException {
        String requestJson = webAuthnRequestCache.getIfPresent(requestId);
        if (requestJson == null) {
            log.error("Request id {} not found in cache", requestId);
            throw new CredentialAssertionFailedException();
        }
        webAuthnRequestCache.invalidate(requestId);

        // For assertion, assume the default Relying Party was used for startAssertion
        RelyingParty relyingParty = buildDefaultRelyingParty();
        log.debug("Finishing assertion for request ID '{}' using default RP ID '{}'", requestId, relyingParty.getIdentity().getId());

        try {
            PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> pkc =
                    PublicKeyCredential.parseAssertionResponseJson(publicKeyCredentialJson);

            AssertionRequest request = AssertionRequest.fromJson(requestJson);
            AssertionResult result = relyingParty.finishAssertion(FinishAssertionOptions.builder()
                    .request(request)  // The AssertionRequest from startAssertion above
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

    // Helper method to build a RelyingParty instance with a specific hostname (rpId)
    private RelyingParty buildRelyingParty(String rpHostname) {
        RelyingPartyIdentity rpIdentity = RelyingPartyIdentity.builder()
                .id(rpHostname)
                .name(relyingPartyProperties.getDisplayName()) // Use configured display name
                .build();

        return RelyingParty.builder()
                .identity(rpIdentity)
                .credentialRepository(databaseCredentialRepository)
                .allowOriginPort(relyingPartyProperties.isAllowOriginPort())
                // Add other configurations like origins if needed, potentially from properties
                // .origins(relyingPartyProperties.getOrigins())
                .build();
    }

    // Helper method to build the default RelyingParty based on configuration properties
    private RelyingParty buildDefaultRelyingParty() {
        return buildRelyingParty(relyingPartyProperties.getHostname());
    }

    // Made public static for potential use elsewhere, like generating IDs before calling startRegistration
    public static ByteArray generateRandom() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return new ByteArray(bytes);
    }
}
