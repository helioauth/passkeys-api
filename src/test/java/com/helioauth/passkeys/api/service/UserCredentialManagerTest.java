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

import com.helioauth.passkeys.api.domain.UserCredential;
import com.helioauth.passkeys.api.domain.UserCredentialRepository;
import com.helioauth.passkeys.api.domain.UserRepository;
import com.helioauth.passkeys.api.generated.models.ListPasskeysResponse;
import com.helioauth.passkeys.api.mapper.UserCredentialMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Viktor Stanchev
 */
@ExtendWith(MockitoExtension.class)
class UserCredentialManagerTest {

    @Mock
    private UserCredentialRepository userCredentialRepository;

    @Mock
    private WebAuthnAuthenticator authenticator;

    @Mock
    private UserRepository userRepository;

    @Spy
    private UserCredentialMapper userCredentialMapper = Mappers.getMapper(UserCredentialMapper.class);

    @InjectMocks
    private UserCredentialManager userCredentialManager;

    @Test
    void getUserCredentials_returnsEmpty_whenResultEmpty() {
        // Arrange
        when(userCredentialRepository.findAllByUserId(any())).thenReturn(Collections.emptyList());

        // Act
        ListPasskeysResponse result = userCredentialManager.getUserCredentials(UUID.randomUUID());

        // Assert
        assertTrue(result.getPasskeys().isEmpty(), "Expected no user credentials");
    }

    @Test
    void getUserCredentials_returnsResult_whenResultNotEmpty() {
        // Arrange
        UUID userUuid = UUID.randomUUID();

        UserCredential credential = UserCredential.builder()
            .id(1L)
            .credentialId(UUID.randomUUID().toString())
            .displayName("Credential Name")
            .createdAt(Instant.now())
            .lastUsedAt(Instant.now())
            .build();

        List<UserCredential> credentialList = Collections.singletonList(credential);

        when(userCredentialRepository.findAllByUserId(userUuid)).thenReturn(credentialList);

        // Act
        ListPasskeysResponse response = userCredentialManager.getUserCredentials(userUuid);

        // Assert
        assertNotNull(response);
        assertFalse(response.getPasskeys().isEmpty(), "The response should not be empty when credentials exist.");
        assertEquals(1, response.getPasskeys().size());
        assertEquals("Credential Name", response.getPasskeys().get(0).getDisplayName());
    }
}