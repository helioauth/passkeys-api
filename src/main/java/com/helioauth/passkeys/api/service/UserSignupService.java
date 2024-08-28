package com.helioauth.passkeys.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.helioauth.passkeys.api.contract.CreateCredentialResponse;
import com.helioauth.passkeys.api.domain.User;
import com.helioauth.passkeys.api.domain.UserCredential;
import com.helioauth.passkeys.api.domain.UserCredentialRepository;
import com.helioauth.passkeys.api.domain.UserRepository;
import com.helioauth.passkeys.api.mapper.UserCredentialMapper;
import com.helioauth.passkeys.api.service.dto.CredentialRegistrationResultDto;
import com.helioauth.passkeys.api.service.exception.SignUpFailedException;
import com.helioauth.passkeys.api.service.exception.UsernameAlreadyRegisteredException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserSignupService {

    private final UserRepository userRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final WebAuthnAuthenticator webAuthnAuthenticator;
    private final UserCredentialMapper usercredentialMapper;

    public CreateCredentialResponse startRegistration(String name) {
        userRepository.findByName(name).ifPresent(_ -> {
            throw new UsernameAlreadyRegisteredException();
        });

        try {
            return webAuthnAuthenticator.startRegistration(name);
        } catch (JsonProcessingException e) {
            log.error("Register Credential failed", e);
            throw new SignUpFailedException();
        }
    }

    @Transactional
    public void finishRegistration(String requestId, String publicKeyCredentialJson) {
        try {
            // TODO fix user handle to be the same for all registered credentials and save in user entity
            CredentialRegistrationResultDto result = webAuthnAuthenticator.finishRegistration(requestId, publicKeyCredentialJson);

            User user = User.builder()
                    .name(result.name())
                    .displayName(result.displayName())
                    .build();
            userRepository.save(user);

            UserCredential userCredential = usercredentialMapper.fromCredentialRegistrationResult(result);
            userCredential.setUser(user);
            userCredentialRepository.save(userCredential);
        } catch (IOException e) {
            log.error("Register Credential failed", e);
            throw new SignUpFailedException();
        }
    }
}
