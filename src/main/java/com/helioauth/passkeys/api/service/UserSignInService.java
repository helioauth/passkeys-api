package com.helioauth.passkeys.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.helioauth.passkeys.api.contract.StartAssertionResponse;
import com.helioauth.passkeys.api.domain.UserCredentialRepository;
import com.helioauth.passkeys.api.service.dto.CredentialAssertionResultDto;
import com.helioauth.passkeys.api.service.exception.SignInFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSignInService {
    private final UserCredentialRepository userCredentialRepository;

    private final WebAuthnAuthenticator webAuthnAuthenticator;

    public StartAssertionResponse startAssertion(String name) throws JsonProcessingException {
        return webAuthnAuthenticator.startAssertion(name);
    }

    @Transactional
    public String finishAssertion(String requestId, String publicKeyCredentialJson) {
        try {
            CredentialAssertionResultDto result = webAuthnAuthenticator.finishAssertion(requestId, publicKeyCredentialJson);

            userCredentialRepository.updateUsageByUserHandleAndCredentialId(
                    result.signatureCount(),
                    result.lastUsedAt(),
                    result.isBackedUp(),
                    result.userHandle(),
                    result.credentialId()
            );

            return result.username();
        } catch (IOException e) {
            log.error("Sign in failed", e);
            throw new SignInFailedException();
        }
    }
}
