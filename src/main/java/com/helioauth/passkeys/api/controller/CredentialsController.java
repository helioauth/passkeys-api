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
import com.helioauth.passkeys.api.contract.SignInFinishRequest;
import com.helioauth.passkeys.api.contract.SignInFinishResponse;
import com.helioauth.passkeys.api.contract.SignInStartRequest;
import com.helioauth.passkeys.api.contract.SignInStartResponse;
import com.helioauth.passkeys.api.contract.SignUpFinishRequest;
import com.helioauth.passkeys.api.contract.SignUpFinishResponse;
import com.helioauth.passkeys.api.contract.SignUpStartRequest;
import com.helioauth.passkeys.api.contract.SignUpStartResponse;
import com.helioauth.passkeys.api.service.UserSignInService;
import com.helioauth.passkeys.api.service.UserSignupService;
import com.helioauth.passkeys.api.service.exception.SignInFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;

/**
 * @author Viktor Stanchev
 */
@Slf4j
@RestController
@RequestMapping("/v1")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CredentialsController {

    private final UserSignInService userSignInService;
    private final UserSignupService userSignupService;

    @PostMapping(value = "/signup/start", produces = MediaType.APPLICATION_JSON_VALUE)
    public SignUpStartResponse postSignupStart(@RequestBody @Valid SignUpStartRequest request) {
        return userSignupService.startRegistration(request.name());
    }

    @PostMapping(value = "/signup/finish", produces = MediaType.APPLICATION_JSON_VALUE)
    public SignUpFinishResponse postSignupFinish(@RequestBody SignUpFinishRequest request) {
        return userSignupService.finishRegistration(request.requestId(), request.publicKeyCredential());
    }

    @PostMapping(value = "/signin/start", produces = MediaType.APPLICATION_JSON_VALUE)
    public SignInStartResponse postSignInCredential(@RequestBody SignInStartRequest request) {
        try {
            return userSignInService.startAssertion(request.name());
        } catch (JsonProcessingException e) {
            log.error("Sign in Credential failed", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sign in Credential failed");
        }
    }

    @PostMapping(value = "/signin/finish", produces = MediaType.APPLICATION_JSON_VALUE)
    public SignInFinishResponse finishSignInCredential(@RequestBody SignInFinishRequest request) {
        try {
            String username = userSignInService.finishAssertion(request.requestId(), request.publicKeyCredentialWithAssertion());
            return new SignInFinishResponse(request.requestId(), username);
        } catch (SignInFailedException e) {
            log.error("Sign in failed", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sign in failed");
        }
    }
}