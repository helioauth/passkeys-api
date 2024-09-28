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
import com.helioauth.passkeys.api.service.dto.ClientApplicationApiKeyDTO;
import com.helioauth.passkeys.api.service.dto.ClientApplicationDTO;
import com.helioauth.passkeys.api.service.ClientApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Viktor Stanchev
 */
@RestController
@RequestMapping("/admin/v1/apps")
@RequiredArgsConstructor
public class ClientApplicationController {

    private final ClientApplicationService clientApplicationService;

    @GetMapping
    public List<ClientApplicationDTO> listAll() {
        return clientApplicationService.listAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientApplicationDTO> get(@PathVariable UUID id) {
        return clientApplicationService.get(id)
                .map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/{id}/api-key")
    public ResponseEntity<ClientApplicationApiKeyDTO> getApiKey(@PathVariable UUID id) {
        return clientApplicationService.getApiKey(id)
                .map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<ClientApplicationDTO> add(@RequestBody AddClientApplicationRequest request) {
        ClientApplicationDTO created = clientApplicationService.add(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientApplicationDTO> edit(@PathVariable UUID id, @RequestBody String name) {
        Optional<ClientApplicationDTO> updated = clientApplicationService.edit(id, name);
        return updated
                .map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        boolean deleted = clientApplicationService.delete(id);
        return deleted ? new ResponseEntity<>(HttpStatus.NO_CONTENT) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}