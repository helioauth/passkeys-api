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

package com.helioauth.passkeys.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.helioauth.passkeys.api.contract.*;
import com.helioauth.passkeys.api.service.UserCredentialManager;
import com.helioauth.passkeys.api.service.UserSignInService;
import com.helioauth.passkeys.api.service.UserSignupService;

import com.helioauth.passkeys.api.service.exception.SignInFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * @author Viktor Stanchev
 */
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

    @PostMapping(value = "/signin/finish", produces = MediaType.APPLICATION_JSON_VALUE)
    public SignInResponse finishSignInCredential(@RequestBody SignInFinishRequest request) {
        try {
            String username = userSignInService.finishAssertion(request.requestId(), request.publicKeyCredentialWithAssertion());
            return new SignInResponse(request.requestId(), username);
        } catch (SignInFailedException e) {
            log.error("Sign in failed", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sign in failed");
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