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

import com.helioauth.passkeys.api.contract.AddClientApplicationRequest;
import com.helioauth.passkeys.api.service.ClientApplicationService;
import com.helioauth.passkeys.api.service.dto.ClientApplicationApiKeyDTO;
import com.helioauth.passkeys.api.service.dto.ClientApplicationDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Viktor Stanchev
 */
@RestController
@RequestMapping("/admin/v1/apps")
@RequiredArgsConstructor
@Tag(name = "Applications", description = "CRUD operations for client applications.")
@SecurityRequirement(name = "admin-api")
public class ClientApplicationController {

    private final ClientApplicationService clientApplicationService;

    @GetMapping
    @Operation(summary = "List all applications", description = "Retrieves a list of all applications.")
    public List<ClientApplicationDTO> listAll() {
        return clientApplicationService.listAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an application", description = "Retrieves details of a specific application by its ID.")
    public ResponseEntity<ClientApplicationDTO> get(@PathVariable UUID id) {
        return clientApplicationService.get(id)
                .map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/{id}/api-key")
    @Operation(summary = "Get an application's API key", description = "Retrieves the API key of a specific application by its ID.")
    public ResponseEntity<ClientApplicationApiKeyDTO> getApiKey(@PathVariable UUID id) {
        return clientApplicationService.getApiKey(id)
                .map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    @Operation(summary = "Add a new application", description = "Creates a new application and returns its details.")
    public ResponseEntity<ClientApplicationDTO> add(@RequestBody AddClientApplicationRequest request) {
        ClientApplicationDTO created = clientApplicationService.add(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edit an application", description = "Updates the name of a specific application by its ID.")
    public ResponseEntity<ClientApplicationDTO> edit(@PathVariable UUID id, @RequestBody String name) {
        Optional<ClientApplicationDTO> updated = clientApplicationService.edit(id, name);
        return updated
                .map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an application", description = "Deletes a specific application by its ID.")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        boolean deleted = clientApplicationService.delete(id);
        return deleted ? new ResponseEntity<>(HttpStatus.NO_CONTENT) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}