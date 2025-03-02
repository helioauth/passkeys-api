package com.helioauth.passkeys.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.helioauth.passkeys.api.domain.User;
import com.helioauth.passkeys.api.domain.UserCredential;
import com.helioauth.passkeys.api.domain.UserCredentialRepository;
import com.helioauth.passkeys.api.domain.UserRepository;
import com.helioauth.passkeys.api.generated.models.SignUpFinishResponse;
import com.helioauth.passkeys.api.generated.models.SignUpStartResponse;
import com.helioauth.passkeys.api.mapper.UserCredentialMapper;
import com.helioauth.passkeys.api.service.dto.AssertionStartResult;
import com.helioauth.passkeys.api.service.dto.CredentialRegistrationResult;
import com.helioauth.passkeys.api.service.exception.SignUpFailedException;
import com.helioauth.passkeys.api.service.exception.UsernameAlreadyRegisteredException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserSignupServiceTest {

    @Mock
    private WebAuthnAuthenticator webAuthnAuthenticator;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private UserCredentialRepository userCredentialRepository;
    
    @Spy
    private UserCredentialMapper userCredentialMapper = Mappers.getMapper(UserCredentialMapper.class);

    @InjectMocks
    private UserSignupService userSignupService;

    @Test
    void testStartRegistration_Success() throws Exception {
        // Arrange
        String name = "testuser";
        AssertionStartResult mockResponse = new AssertionStartResult("requestId123", "{\"options\":\"value\"}");
        when(webAuthnAuthenticator.startRegistration(name)).thenReturn(mockResponse);

        // Act
        SignUpStartResponse response = userSignupService.startRegistration(name);

        // Assert
        assertNotNull(response);
        assertEquals("requestId123", response.getRequestId());
        assertEquals("{\"options\":\"value\"}", response.getOptions());
        verify(webAuthnAuthenticator, times(1)).startRegistration(name);
    }

    @Test
    void testStartRegistration_ThrowsSignUpFailedException() throws Exception {
        // Arrange
        String name = "testuser";
        when(webAuthnAuthenticator.startRegistration(name)).thenThrow(JsonProcessingException.class);

        // Act & Assert
        assertThrows(SignUpFailedException.class, () -> userSignupService.startRegistration(name));
    }

    @Test
    void testFinishRegistration_Success() throws Exception {
        // Arrange
        String requestId = "requestId123";
        String publicKeyCredentialJson = "{\"key\":\"value\"}";
        String username = "testuser";
        String displayName = "Test User";

        CredentialRegistrationResult mockResult = new CredentialRegistrationResult(
            username, displayName,
            "test", "test", 0L,
            "", "","",
            true, true, true
        );
        
        User mockUser = User.builder()
            .id(UUID.randomUUID())
            .name(username)
            .displayName(displayName)
            .build();
        UserCredential mockCredential = new UserCredential();

        when(webAuthnAuthenticator.getUsernameByRequestId(requestId)).thenReturn(username);
        when(userRepository.findByName(username)).thenReturn(Optional.empty());
        when(webAuthnAuthenticator.finishRegistration(requestId, publicKeyCredentialJson)).thenReturn(mockResult);
        when(userCredentialRepository.save(any(UserCredential.class))).thenReturn(mockCredential);
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // Act
        SignUpFinishResponse response = userSignupService.finishRegistration(requestId, publicKeyCredentialJson);

        // Assert
        assertNotNull(response);
        assertEquals(requestId, response.getRequestId());
        assertNotNull(response.getUserId());
    }

    @Test
    void testFinishRegistration_ThrowsUsernameAlreadyRegisteredException() throws Exception {
        // Arrange
        String requestId = "requestId123";
        String publicKeyCredentialJson = "{\"key\":\"value\"}";
        String username = "testuser";
        User existingUser = User.builder().name(username).build();

        when(webAuthnAuthenticator.getUsernameByRequestId(requestId)).thenReturn(username);
        when(userRepository.findByName(username)).thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThrows(UsernameAlreadyRegisteredException.class,
            () -> userSignupService.finishRegistration(requestId, publicKeyCredentialJson)
        );

        verify(webAuthnAuthenticator, never()).finishRegistration(anyString(), anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(userCredentialRepository, never()).save(any(UserCredential.class));
    }

    @Test
    void testFinishRegistration_ThrowsSignUpFailedException() throws Exception {
        // Arrange
        String requestId = "requestId123";
        String publicKeyCredentialJson = "{\"key\":\"value\"}";

        when(webAuthnAuthenticator.getUsernameByRequestId(requestId)).thenThrow(IOException.class);

        // Act & Assert
        assertThrows(SignUpFailedException.class,
            () -> userSignupService.finishRegistration(requestId, publicKeyCredentialJson)
        );

        verify(webAuthnAuthenticator, never()).finishRegistration(anyString(), anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(userCredentialRepository, never()).save(any(UserCredential.class));
    }
}