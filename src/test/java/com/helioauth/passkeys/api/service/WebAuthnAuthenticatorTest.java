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
import com.helioauth.passkeys.api.mapper.CredentialRegistrationResultMapper;
import com.helioauth.passkeys.api.service.dto.AssertionStartResult;
import com.helioauth.passkeys.api.service.dto.CredentialRegistrationResult;
import com.helioauth.passkeys.api.service.exception.CredentialRegistrationFailedException;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.data.ByteArray;
import com.helioauth.passkeys.api.webauthn.DatabaseCredentialRepository;
import com.helioauth.passkeys.api.mapper.RegistrationResponseMapper;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.data.exception.HexException;
import com.yubico.webauthn.exception.RegistrationFailedException;
import com.helioauth.passkeys.api.config.properties.WebAuthnRelyingPartyProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WebAuthnAuthenticatorTest {

    private static final String TEST_USER_NAME = "test3";
    private static final ByteArray TEST_USER_ID = new ByteArray(new byte[]{1, 2, 3});
    private static final String TEST_RP_ID = "localhost";
    private static final String TEST_RP_NAME = "Test RP";
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
    private Cache<String, String> webAuthnRequestCache;

    @Spy
    private WebAuthnRelyingPartyProperties relyingPartyProperties = new WebAuthnRelyingPartyProperties();

    @Mock
    private DatabaseCredentialRepository databaseCredentialRepository;

    @Spy
    private CredentialRegistrationResultMapper credentialRegistrationResultMapper = Mappers.getMapper(CredentialRegistrationResultMapper.class);

    @Spy
    private RegistrationResponseMapper registrationResponseMapper = Mappers.getMapper(RegistrationResponseMapper.class);

    @InjectMocks
    private WebAuthnAuthenticator authenticator;

    @BeforeEach
    void setUp() {
        relyingPartyProperties.setHostname(TEST_RP_ID);
        relyingPartyProperties.setDisplayName(TEST_RP_NAME);
        relyingPartyProperties.setAllowOriginPort(true);
    }

    @Test
    public void testStartRegistrationWithNameAndUserId() throws JsonProcessingException, HexException {
        AssertionStartResult response = authenticator.startRegistration(TEST_USER_NAME, TEST_USER_ID, TEST_RP_ID);

        verify(webAuthnRequestCache, times(1)).put(anyString(), anyString());
        assertNotNull(response);
        assertNotNull(response.requestId());
        assertNotNull(response.options());

        assertTrue(response.options().contains("\"id\":\"" + TEST_RP_ID + "\""));
        assertTrue(response.options().contains("\"name\":\"" + TEST_RP_NAME + "\""));
    }

    @Test
    public void testStartRegistrationWithName() throws JsonProcessingException, HexException {
        AssertionStartResult response = authenticator.startRegistration(TEST_USER_NAME);

        verify(webAuthnRequestCache, times(1)).put(anyString(), anyString());
        assertNotNull(response);
        assertNotNull(response.requestId());
        assertNotNull(response.options());
        assertTrue(response.options().contains("\"id\":\"" + TEST_RP_ID + "\""));
        assertTrue(response.options().contains("\"name\":\"" + TEST_RP_NAME + "\""));
    }

    @Test
    public void testFinishRegistrationSuccess() throws IOException, RegistrationFailedException {
        String requestId = "requestId";

        CredentialRegistrationResult mappedResult = new CredentialRegistrationResult("name", "displayName", "credentialId", "userHandle", 1L, "publicKeyCose",
                "attestationObject", "clientDataJson", true, true, true);

        doReturn(mappedResult).when(credentialRegistrationResultMapper)
            .fromRegistrationResult(
                any(RegistrationResult.class),
                any(UserIdentity.class),
                any(AuthenticatorAttestationResponse.class)
            );

        when(webAuthnRequestCache.getIfPresent(requestId)).thenReturn(AUTHENTICATOR_REQUEST_JSON);

        CredentialRegistrationResult result = authenticator.finishRegistration(requestId, AUTHENTICATOR_RESPONSE_JSON);

        assertNotNull(result);
        assertEquals(mappedResult.name(), result.name());
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

        doThrow(new RuntimeException("Simulated mapping error")).when(credentialRegistrationResultMapper)
            .fromRegistrationResult(
                any(RegistrationResult.class),
                any(UserIdentity.class),
                any(AuthenticatorAttestationResponse.class) // Use specific type
            );

        CredentialRegistrationFailedException exception = assertThrows(
            CredentialRegistrationFailedException.class,
            () -> authenticator.finishRegistration(requestId, AUTHENTICATOR_RESPONSE_JSON)
        );
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("Failed to finish registration")); // Check message
        assertNotNull(exception.getCause()); // Check that the original cause is wrapped
        assertEquals("Simulated mapping error", exception.getCause().getMessage());

        verify(webAuthnRequestCache, times(1)).invalidate(requestId);
    }
}
