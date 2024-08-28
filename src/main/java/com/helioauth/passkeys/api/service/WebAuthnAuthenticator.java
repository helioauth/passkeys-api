package com.helioauth.passkeys.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.helioauth.passkeys.api.contract.CreateCredentialResponse;
import com.helioauth.passkeys.api.contract.StartAssertionResponse;
import com.helioauth.passkeys.api.mapper.CredentialRegistrationResultMapper;
import com.helioauth.passkeys.api.service.dto.CredentialAssertionResultDto;
import com.helioauth.passkeys.api.service.dto.CredentialRegistrationResultDto;
import com.helioauth.passkeys.api.service.exception.CredentialAssertionFailedException;
import com.helioauth.passkeys.api.service.exception.CredentialRegistrationFailedException;
import com.yubico.webauthn.*;
import com.yubico.webauthn.data.*;
import com.yubico.webauthn.exception.AssertionFailedException;
import com.yubico.webauthn.exception.RegistrationFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebAuthnAuthenticator {

    private final RelyingParty relyingParty;

    private final Cache<String, String> cache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(5))
            .maximumSize(1000)
            .build();

    private static final SecureRandom random = new SecureRandom();

    private final CredentialRegistrationResultMapper credentialRegistrationResultMapper;

    public CreateCredentialResponse startRegistration(String name) throws JsonProcessingException {
        ByteArray id = generateRandom(32);

        ResidentKeyRequirement residentKeyRequirement = ResidentKeyRequirement.PREFERRED;

        PublicKeyCredentialCreationOptions request = relyingParty.startRegistration(StartRegistrationOptions.builder()
            .user(
                UserIdentity.builder()
                    .name(name)
                    .displayName(name)
                    .id(id)
                    .build()
            )
            .authenticatorSelection(
                    AuthenticatorSelectionCriteria.builder()
                            .residentKey(residentKeyRequirement)
                            .build()
            )
            .build()
        );

        String requestId = id.getHex();
        cache.put(requestId, request.toJson());

        return CreateCredentialResponse.builder()
                .requestId(requestId)
                .publicKeyCredentialCreationOptions(request.toCredentialsCreateJson())
                .build();
    }

    public CredentialRegistrationResultDto finishRegistration(String requestId, String publicKeyCredentialJson) throws IOException {
        PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> pkc =
                PublicKeyCredential.parseRegistrationResponseJson(publicKeyCredentialJson);

        String requestJson = cache.getIfPresent(requestId);
        if (requestJson == null) {
            throw new CredentialRegistrationFailedException();
        }
        cache.invalidate(requestId);

        try {
            PublicKeyCredentialCreationOptions request = PublicKeyCredentialCreationOptions.fromJson(requestJson);

            RegistrationResult result = relyingParty.finishRegistration(FinishRegistrationOptions.builder()
                    .request(request)
                    .response(pkc)
                    .build());

            return credentialRegistrationResultMapper.fromRegistrationResult(result, request.getUser(), pkc.getResponse());

        } catch (RegistrationFailedException e) {
            throw new CredentialRegistrationFailedException();
        }
    }

    public StartAssertionResponse startAssertion(String name) throws JsonProcessingException {
        AssertionRequest request = relyingParty.startAssertion(StartAssertionOptions.builder()
                .username(name)
                .build());

        String requestId = generateRandom(32).getHex();
        cache.put(requestId, request.toJson());

        return new StartAssertionResponse(requestId, request.toCredentialsGetJson());
    }

    public CredentialAssertionResultDto finishAssertion(String requestId, String publicKeyCredentialJson) throws IOException {
        String requestJson = cache.getIfPresent(requestId);
        if (requestJson == null) {
            log.error("Request id {} not found in cache", requestId);
            throw new CredentialAssertionFailedException();
        }
        cache.invalidate(requestId);

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

                return new CredentialAssertionResultDto(
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

    private static ByteArray generateRandom(int length) {
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return new ByteArray(bytes);
    }
}