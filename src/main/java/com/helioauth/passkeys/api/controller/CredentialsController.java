package com.helioauth.passkeys.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.helioauth.passkeys.api.contract.*;
import com.helioauth.passkeys.api.service.UserCredentialManager;
import com.helioauth.passkeys.api.service.UserSignInService;
import com.helioauth.passkeys.api.service.UserSignupService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/v1/credentials")
@Slf4j
@RequiredArgsConstructor
public class CredentialsController {

    private final UserSignInService userSignInService;
    private final UserSignupService userSignupService;
    private final UserCredentialManager userCredentialManager;

    @PostMapping(value = "/signup/start", produces = MediaType.APPLICATION_JSON_VALUE)
    public CreateCredentialResponse postCreateCredential(@RequestBody CreateCredentialRequest createCredentialRequest) {
        return userSignupService.startRegistration(createCredentialRequest.name());
    }

    @PostMapping(value = "/signup/finish", produces = MediaType.APPLICATION_JSON_VALUE)
    public FinishCredentialCreateResponse postRegisterCredential(@RequestBody RegisterCredentialRequest request) {
        userSignupService.finishRegistration(request.requestId(), request.publicKeyCredential());
        return new FinishCredentialCreateResponse(request.requestId());
    }

    @PostMapping(value = "/signin/start", produces = MediaType.APPLICATION_JSON_VALUE)
    public StartAssertionResponse postSignInCredential(@RequestBody StartAssertionRequest startAssertionRequest) {
        try {
            return userSignInService.startAssertion(startAssertionRequest.name());
        } catch (JsonProcessingException e) {
            log.error("Sign in Credential failed", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sign in Credential failed");
        }
    }

    @PostMapping(value = "/add/start", produces = MediaType.APPLICATION_JSON_VALUE)
    public CreateCredentialResponse credentialsAddStart(@RequestBody String username) {
        return userCredentialManager.createCredential(username);
    }

    @PostMapping(value = "/add/finish", produces = MediaType.APPLICATION_JSON_VALUE)
    public FinishCredentialCreateResponse credentialsAddFinish(@RequestBody RegisterCredentialRequest request) {
        userCredentialManager.finishCreateCredential(request);
        return new FinishCredentialCreateResponse(request.requestId());
    }
}