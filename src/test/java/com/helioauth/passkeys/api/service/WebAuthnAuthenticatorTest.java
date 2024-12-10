package com.helioauth.passkeys.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.benmanes.caffeine.cache.Cache;
import com.helioauth.passkeys.api.generated.models.SignUpStartResponse;
import com.helioauth.passkeys.api.mapper.CredentialRegistrationResultMapper;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.data.exception.HexException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WebAuthnAuthenticatorTest {

    public static final String TEST_USER_NAME = "testUser";
    public static final ByteArray TEST_USER_ID = new ByteArray(new byte[]{1, 2, 3});
    public static final UserIdentity USER_IDENTITY = UserIdentity.builder()
        .name(TEST_USER_NAME)
        .displayName(TEST_USER_NAME)
        .id(TEST_USER_ID)
        .build();
    @Mock
    private RelyingParty relyingParty;

    @Mock
    private Cache<String, String> webAuthnRequestCache;

    @Spy
    private CredentialRegistrationResultMapper credentialRegistrationResultMapper = Mappers.getMapper(CredentialRegistrationResultMapper.class);

    @InjectMocks
    private WebAuthnAuthenticator authenticator;
    
    @BeforeEach
    public void setUp() {
        when(relyingParty.getIdentity()).thenReturn(
            RelyingPartyIdentity.builder().id("testId").name("testName").build()
        );
    }

    @Test
    public void testStartRegistrationWithNameAndUserId() throws JsonProcessingException, HexException {
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
}