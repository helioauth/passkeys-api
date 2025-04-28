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

import com.helioauth.passkeys.api.generated.api.ApplicationsApi;
import com.helioauth.passkeys.api.generated.models.AddApplicationRequest;
import com.helioauth.passkeys.api.generated.models.Application;
import com.helioauth.passkeys.api.generated.models.ApplicationApiKey;
import com.helioauth.passkeys.api.generated.models.EditApplicationRequest;
import com.helioauth.passkeys.api.service.ClientApplicationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * @author Viktor Stanchev
 */
@RestController
@RequiredArgsConstructor
public class ClientApplicationController implements ApplicationsApi {

 private final ClientApplicationService clientApplicationService;

 public ResponseEntity<List<Application>> listAll() {
        return ResponseEntity.ok(clientApplicationService.listAll());
    }

    public ResponseEntity<Application> get(@PathVariable UUID id) {
        return clientApplicationService.get(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public ResponseEntity<ApplicationApiKey> getApiKey(@PathVariable UUID id) {
        return clientApplicationService.getApiKey(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public ResponseEntity<Application> add(@RequestBody AddApplicationRequest request) {
        Application created = clientApplicationService.add(request);

        return ResponseEntity.created(URI.create("/admin/v1/apps/" + created.getId()))
            .body(created);
    }

    public ResponseEntity<Application> edit(@PathVariable UUID id, @RequestBody @Valid EditApplicationRequest request) {
        val updated = clientApplicationService.edit(id, request);

        return updated
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        boolean deleted = clientApplicationService.delete(id);

        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}