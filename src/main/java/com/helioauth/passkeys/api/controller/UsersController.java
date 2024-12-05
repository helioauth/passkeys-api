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

import com.helioauth.passkeys.api.generated.api.UsersApi;
import com.helioauth.passkeys.api.generated.models.ListPasskeysResponse;
import com.helioauth.passkeys.api.service.UserAccountManager;
import com.helioauth.passkeys.api.service.UserCredentialManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * @author Viktor Stanchev
 */
@RestController
@RequestMapping("/v1/users")
@Slf4j
@RequiredArgsConstructor
public class UsersController implements UsersApi {
    private final UserCredentialManager userCredentialManager;
    private final UserAccountManager userAccountManager;

    @GetMapping("/{uuid}/credentials")
    public ResponseEntity<ListPasskeysResponse> getUserCredentials(@PathVariable UUID uuid) {
        return ResponseEntity.ok(userCredentialManager.getUserCredentials(uuid));
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID uuid) {
        userAccountManager.deleteUser(uuid);
        return ResponseEntity.noContent().build();
    }
}
