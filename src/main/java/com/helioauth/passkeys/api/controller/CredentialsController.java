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
import com.helioauth.passkeys.api.generated.api.SignInApi;
import com.helioauth.passkeys.api.generated.api.SignUpApi;
import com.helioauth.passkeys.api.generated.models.SignInFinishRequest;
import com.helioauth.passkeys.api.generated.models.SignInFinishResponse;
import com.helioauth.passkeys.api.generated.models.SignInStartRequest;
import com.helioauth.passkeys.api.generated.models.SignInStartResponse;
import com.helioauth.passkeys.api.generated.models.SignUpFinishRequest;
import com.helioauth.passkeys.api.generated.models.SignUpFinishResponse;
import com.helioauth.passkeys.api.generated.models.SignUpStartRequest;
import com.helioauth.passkeys.api.generated.models.SignUpStartResponse;
import com.helioauth.passkeys.api.service.UserSignInService;
import com.helioauth.passkeys.api.service.UserSignupService;
import com.helioauth.passkeys.api.service.exception.SignInFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;

/**
 * @author Viktor Stanchev
 */
@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CredentialsController implements SignUpApi, SignInApi {

    private final UserSignInService userSignInService;
    private final UserSignupService userSignupService;

    public ResponseEntity<SignUpStartResponse> postSignupStart(@RequestBody @Valid SignUpStartRequest request) {
        return ResponseEntity.ok(
            userSignupService.startRegistration(request.getName())
        );
    }

    public ResponseEntity<SignUpFinishResponse> postSignupFinish(@RequestBody SignUpFinishRequest request) {
        return ResponseEntity.ok(
            userSignupService.finishRegistration(request.getRequestId(), request.getPublicKeyCredential())
        );
    }

    public ResponseEntity<SignInStartResponse> postSignInCredential(@RequestBody SignInStartRequest request) {
        try {
            return ResponseEntity.ok(
                userSignInService.startAssertion(request.getName())
            );
        } catch (JsonProcessingException e) {
            log.error("Sign in Credential failed", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sign in Credential failed");
        }
    }

    public ResponseEntity<SignInFinishResponse> finishSignInCredential(@RequestBody SignInFinishRequest request) {
        try {
            String username = userSignInService.finishAssertion(request.getRequestId(), request.getPublicKeyCredentialWithAssertion());

            return ResponseEntity.ok(
                new SignInFinishResponse(request.getRequestId(), username)
            );
        } catch (SignInFailedException e) {
            log.error("Sign in failed", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sign in failed");
        }
    }
}