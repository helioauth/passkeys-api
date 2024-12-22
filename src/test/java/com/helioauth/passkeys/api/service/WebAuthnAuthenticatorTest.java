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
import com.helioauth.passkeys.api.generated.models.SignUpStartResponse;
import com.helioauth.passkeys.api.mapper.CredentialRegistrationResultMapper;
import com.helioauth.passkeys.api.service.dto.CredentialRegistrationResult;
import com.helioauth.passkeys.api.service.exception.CredentialRegistrationFailedException;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.data.exception.HexException;
import com.yubico.webauthn.exception.RegistrationFailedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WebAuthnAuthenticatorTest {

    private static final String TEST_USER_NAME = "test3";
    private static final ByteArray TEST_USER_ID = new ByteArray(new byte[]{1, 2, 3});
    private static final UserIdentity USER_IDENTITY = UserIdentity.builder()
        .name(TEST_USER_NAME)
        .displayName(TEST_USER_NAME)
        .id(TEST_USER_ID)
        .build();

    private static final String AUTHENTICATOR_REQUEST_JSON = """
        {
            "rp": { "name": "HelioAuth Passkeys API", "id": "localhost" },
            "user": { "name": "test3@example.com", "displayName": "test3@example.com", "id": "VocUvpS_E_-xD41LO8ej5zVAxZuRw5dbtnIMsVWtso4" },
            "challenge": "UWetJgxUIl_u7Dn6rF8_kvN9IeGj227Pc6i59MuUOw8",
            "pubKeyCredParams": [
                { "alg": -7, "type": "public-key" },
                { "alg": -8, "type": "public-key" },
                { "alg": -35, "type": "public-key" },
                { "alg": -36, "type": "public-key" },
                { "alg": -257, "type": "public-key" },
                { "alg": -258, "type": "public-key" },
                { "alg": -259, "type": "public-key" }
            ],
            "excludeCredentials": [],
            "authenticatorSelection": { "requireResidentKey": false, "residentKey": "preferred" },
            "attestation": "none",
            "extensions": { "credProps": true }
        }
        """;

    private static final String AUTHENTICATOR_RESPONSE_JSON = """
    {
        "type":"public-key",
        "id":"kUaBI-MmRg6NmRupGNA_Tx0JOnyKF1qZCs9FkqpAMv8",
        "rawId":"kUaBI-MmRg6NmRupGNA_Tx0JOnyKF1qZCs9FkqpAMv8",
        "authenticatorAttachment":"platform",
        "response": {
            "clientDataJSON":"eyJ0eXBlIjoid2ViYXV0aG4uY3JlYXRlIiwiY2hhbGxlbmdlIjoiVVdldEpneFVJbF91N0RuNnJGOF9rdk45SWVHajIyN1BjNmk1OU11VU93OCIsIm9yaWdpbiI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODA4MSIsImNyb3NzT3JpZ2luIjpmYWxzZX0",
            "attestationObject":"o2NmbXRkbm9uZWdhdHRTdG10oGhhdXRoRGF0YVikSZYN5YgOjGh0NBcPZHZgW4_krrmihjLHmVzzuoMdl2NFAAAAAQECAwQFBgcIAQIDBAUGBwgAIJFGgSPjJkYOjZkbqRjQP08dCTp8ihdamQrPRZKqQDL_pQECAyYgASFYIMri0ZKX3-DsEsvFSkRlLfXskw9KK2nS99vZmw85z_O4Ilgg4ZO7yiq0dSWs2S4CN4bBvzvWoyCjF1-F_PUWGjTcv-g",
            "transports": ["internal"]
        },
        "clientExtensionResults": { "credProps": { "rk": true } }
    }""";

    @Mock
    private RelyingParty relyingParty;

    @Mock
    private Cache<String, String> webAuthnRequestCache;

    @Spy
    private CredentialRegistrationResultMapper credentialRegistrationResultMapper = Mappers.getMapper(CredentialRegistrationResultMapper.class);

    @InjectMocks
    private WebAuthnAuthenticator authenticator;
    
    public void setUpRelyingPartyIdentityMock() {
        when(relyingParty.getIdentity()).thenReturn(
            RelyingPartyIdentity.builder().id("testId").name("testName").build()
        );
    }

    @Test
    public void testStartRegistrationWithNameAndUserId() throws JsonProcessingException, HexException {
        setUpRelyingPartyIdentityMock();

        PublicKeyCredentialCreationOptions pkcco = PublicKeyCredentialCreationOptions.builder()
            .rp(relyingParty.getIdentity())
            .user(USER_IDENTITY)
            .challenge(ByteArray.fromHex("1234567890abcdef"))
            .pubKeyCredParams(List.of())
            .build();

        when(relyingParty.startRegistration(any(StartRegistrationOptions.class))).thenReturn(pkcco);

        // Execute the test
        SignUpStartResponse response = authenticator.startRegistration(TEST_USER_NAME, TEST_USER_ID);

        // Verify interactions and assert results
        verify(relyingParty, times(1)).startRegistration(any(StartRegistrationOptions.class));
        verify(webAuthnRequestCache, times(1)).put(anyString(), anyString());
        assertNotNull(response);
        assertNotNull(response.getRequestId());
        assertNotNull(response.getOptions());
    }

    @Test
    public void testStartRegistrationWithName() throws JsonProcessingException, HexException {
        setUpRelyingPartyIdentityMock();

        PublicKeyCredentialCreationOptions pkcco = PublicKeyCredentialCreationOptions.builder()
            .rp(relyingParty.getIdentity())
            .user(USER_IDENTITY)
            .challenge(ByteArray.fromHex("1234567890abcdef"))
            .pubKeyCredParams(List.of())
            .build();

        when(relyingParty.startRegistration(any(StartRegistrationOptions.class))).thenReturn(pkcco);

        // Execute the test
        SignUpStartResponse response = authenticator.startRegistration(TEST_USER_NAME);

        // Verify interactions and assert results
        verify(relyingParty, times(1)).startRegistration(any(StartRegistrationOptions.class));
        verify(webAuthnRequestCache, times(1)).put(anyString(), anyString());
        assertNotNull(response);
        assertNotNull(response.getRequestId());
        assertNotNull(response.getOptions());
    }

    @Test
    public void testFinishRegistrationSuccess() throws IOException, RegistrationFailedException {
        String requestId = "requestId";

        RegistrationResult mockResult = mock( RegistrationResult.class);

        when(webAuthnRequestCache.getIfPresent(requestId)).thenReturn(AUTHENTICATOR_REQUEST_JSON);
        when(relyingParty.finishRegistration(any(FinishRegistrationOptions.class))).thenReturn(mockResult);
        when(credentialRegistrationResultMapper.fromRegistrationResult(any(), any(), any()))
            .thenReturn(new CredentialRegistrationResult("name", "displayName", "credentialId", "userHandle", 1L, "publicKeyCose",
                "attestationObject", "clientDataJson", true, true, true));

        CredentialRegistrationResult result = authenticator.finishRegistration(requestId, AUTHENTICATOR_RESPONSE_JSON);

        assertNotNull(result);
        assertNotNull(result.name());
        verify(webAuthnRequestCache, times(1)).invalidate(requestId);
    }

    @Test
    public void testFinishRegistrationRequestIdNotFound() {
        String requestId = "nonexistentId";
        String responseJson = "{}";

        when(webAuthnRequestCache.getIfPresent(requestId)).thenReturn(null);

        assertThrows(
            CredentialRegistrationFailedException.class,
            () -> authenticator.finishRegistration(requestId, responseJson)
        );

        verify(webAuthnRequestCache, times(0)).invalidate(requestId);
    }

    @Test
    public void testFinishRegistrationThrowsException() throws RegistrationFailedException {
        String requestId = "requestId";

        when(webAuthnRequestCache.getIfPresent(requestId)).thenReturn(AUTHENTICATOR_REQUEST_JSON);
        when(relyingParty.finishRegistration(any(FinishRegistrationOptions.class)))
            .thenThrow(new RegistrationFailedException(new IllegalArgumentException()));

        CredentialRegistrationFailedException exception = assertThrows(
            CredentialRegistrationFailedException.class,
            () -> authenticator.finishRegistration(requestId, AUTHENTICATOR_RESPONSE_JSON)
        );
        assertNotNull(exception.getMessage());
        
        verify(webAuthnRequestCache, times(1)).invalidate(requestId);
    }
}