package com.helioauth.passkeys.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.helioauth.passkeys.api.contract.CreateCredentialResponse;
import com.helioauth.passkeys.api.contract.RegisterCredentialRequest;
import com.helioauth.passkeys.api.domain.User;
import com.helioauth.passkeys.api.domain.UserCredential;
import com.helioauth.passkeys.api.domain.UserCredentialRepository;
import com.helioauth.passkeys.api.domain.UserRepository;
import com.helioauth.passkeys.api.mapper.UserCredentialMapper;
import com.helioauth.passkeys.api.service.dto.CredentialRegistrationResultDto;
import com.helioauth.passkeys.api.service.exception.CreateCredentialFailedException;
import com.helioauth.passkeys.api.service.exception.SignUpFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCredentialManager {
    private final WebAuthnAuthenticator webAuthnAuthenticator;
    private final UserRepository userRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final UserCredentialMapper userCredentialMapper;

    public CreateCredentialResponse createCredential(String name) {
        try {
            return webAuthnAuthenticator.startRegistration(name);
        } catch (JsonProcessingException e) {
            log.error("Creating a new credential failed", e);
            throw new CreateCredentialFailedException();
        }
    }

    public void finishCreateCredential(RegisterCredentialRequest request) {
        try {
            CredentialRegistrationResultDto result = webAuthnAuthenticator.finishRegistration(
                    request.requestId(),
                    request.publicKeyCredential()
            );

            User user = userRepository.findByName(request.username()).orElseThrow(CreateCredentialFailedException::new);

            UserCredential userCredential = userCredentialMapper.fromCredentialRegistrationResult(result);
            userCredential.setUser(user);
            userCredentialRepository.save(userCredential);
        } catch (IOException e) {
            log.error("Register Credential failed", e);
            throw new SignUpFailedException();
        }
    }
}
