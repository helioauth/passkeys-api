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

import com.helioauth.passkeys.api.contract.AddClientApplicationRequest;
import com.helioauth.passkeys.api.domain.ClientApplication;
import com.helioauth.passkeys.api.mapper.ClientApplicationMapper;
import com.helioauth.passkeys.api.service.dto.ClientApplicationApiKeyDTO;
import com.helioauth.passkeys.api.service.dto.ClientApplicationDTO;
import com.helioauth.passkeys.api.domain.ClientApplicationRepository;
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

    private final ClientApplicationMapper mapper;

    private final SecureRandom random = new SecureRandom();

    public Optional<ClientApplicationDTO> get(UUID id) {
        return repository.findById(id).map(mapper::toDto);
    }

    public Optional<ClientApplicationApiKeyDTO> getApiKey(UUID id) {
        return repository.findById(id).map(mapper::toApiKeyDto);
    }

    public List<ClientApplicationDTO> listAll() {
        return mapper.toDto(
            repository.findAll()
        );
    }

    public ClientApplicationDTO add(AddClientApplicationRequest dto) {
        return mapper.toDto(
            repository.save(
                new ClientApplication(dto.name(), generateApiKey())
            )
        );
    }

    @Transactional
    public Optional<ClientApplicationDTO> edit(UUID id, String name) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setName(name);
                    return mapper.toDto(repository.save(existing));
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