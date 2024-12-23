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

package com.helioauth.passkeys.api.service;

import com.helioauth.passkeys.api.domain.ClientApplication;
import com.helioauth.passkeys.api.domain.ClientApplicationRepository;
import com.helioauth.passkeys.api.generated.models.Application;
import com.helioauth.passkeys.api.generated.models.ApplicationApiKey;
import com.helioauth.passkeys.api.mapper.ClientApplicationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Viktor Stanchev
 */
@Service
@RequiredArgsConstructor
public class ClientApplicationService {

    private final ClientApplicationRepository repository;

    private final ClientApplicationMapper clientApplicationMapper;

    private final SecureRandom random = new SecureRandom();

    public Optional<Application> get(UUID id) {

        return repository.findById(id).map(clientApplicationMapper::toResponse);
    }

    public Optional<ApplicationApiKey> getApiKey(UUID id) {
        return repository.findById(id).map(clientApplicationMapper::toApiKeyResponse);
    }

    public List<Application> listAll() {

        return clientApplicationMapper.toResponse(
            repository.findAll()
        );
    }

    public Application add(String name) {

        return clientApplicationMapper.toResponse(
            repository.save(
                new ClientApplication(name, generateApiKey())
            )
        );
    }

    @Transactional
    public Optional<Application> edit(UUID id, String name) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setName(name);
                    return clientApplicationMapper.toResponse(repository.save(existing));
                });
    }

    @Transactional
    public boolean delete(UUID id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    private String generateApiKey() {
        byte[] buffer = new byte[16];
        random.nextBytes(buffer);
        StringBuilder sb = new StringBuilder();
        for (byte b : buffer) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}