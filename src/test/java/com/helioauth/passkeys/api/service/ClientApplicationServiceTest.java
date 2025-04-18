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
import com.helioauth.passkeys.api.generated.models.AddApplicationRequest;
import com.helioauth.passkeys.api.generated.models.Application;
import com.helioauth.passkeys.api.generated.models.ApplicationApiKey;
import com.helioauth.passkeys.api.generated.models.EditApplicationRequest;
import com.helioauth.passkeys.api.mapper.ClientApplicationMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ClientApplicationServiceTest {

    public static final Application DTO = new Application()
        .id(UUID.randomUUID())
        .name("App Name")
        .createdAt(Instant.now())
        .updatedAt(Instant.now());

    public static final ClientApplication CLIENT_APPLICATION = ClientApplication.builder()
        .id(DTO.getId())
        .name(DTO.getName())
        .createdAt(DTO.getCreatedAt())
        .updatedAt(DTO.getUpdatedAt())
        .build();

    @Mock
    private ClientApplicationRepository repository;

    @Spy
    private ClientApplicationMapper mapper = Mappers.getMapper( ClientApplicationMapper.class);

    @InjectMocks
    private ClientApplicationService service;

    @Test
    public void addClientApplicationTest() {
        // Setup
        ArgumentCaptor<ClientApplication> argumentCaptor = ArgumentCaptor.forClass(ClientApplication.class);
        when(repository.save(any(ClientApplication.class))).thenReturn(CLIENT_APPLICATION);

        // Execute
        AddApplicationRequest addApplicationRequest = new AddApplicationRequest();
        addApplicationRequest.setName(DTO.getName());
        Application result = service.add(addApplicationRequest);

        // Capture the argument
        verify(repository).save(argumentCaptor.capture());
        ClientApplication savedClientApplication = argumentCaptor.getValue();

        // Validate
        assertFalse(savedClientApplication.getApiKey().isEmpty());
        assertEquals(DTO.getName(), savedClientApplication.getName());
        assertEquals(DTO.getName(), result.getName());
    }

    @Test
    public void listAllClientApplicationsTest() {
        // Setup
        List<ClientApplication> applications = List.of(CLIENT_APPLICATION);
        when(repository.findAll()).thenReturn(applications);

        // Execute
        List<Application> result = service.listAll();

        // Validate
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(DTO.getId(), result.get(0).getId());
        assertEquals(DTO.getName(), result.get(0).getName());
    }

    @Test
    public void editClientApplicationTest() {
        // Setup
        UUID id = UUID.randomUUID();
        String newName = "Updated Name";
        ClientApplication existingClientApplication = ClientApplication.builder()
            .id(id)
            .name("Old Name")
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        when(repository.findById(id)).thenReturn(Optional.of(existingClientApplication));
        when(repository.save(any(ClientApplication.class))).thenReturn(existingClientApplication);

        // Execute
        EditApplicationRequest editApplicationRequest = new EditApplicationRequest();
        editApplicationRequest.setName(newName);
        Optional<Application> result = service.edit(id, editApplicationRequest);

        // Validate
        assertTrue(result.isPresent());
        assertEquals(newName, result.get().getName());
    }

    @Test
    public void deleteClientApplicationTest() {
        // Setup
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(true);

        // Execute
        boolean result = service.delete(id);

        // Validate
        assertTrue(result);
        when(repository.existsById(id)).thenReturn(false);
        result = service.delete(id);
        assertFalse(result);
    }

    @Test
    public void getClientApplicationTest() {
        // Setup
        UUID id = DTO.getId();
        when(repository.findById(id)).thenReturn(Optional.of(CLIENT_APPLICATION));

        // Execute
        Optional<Application> result = service.get(id);

        // Validate
        assertTrue(result.isPresent());
        assertEquals(DTO.getId(), result.get().getId());
        assertEquals(DTO.getName(), result.get().getName());
    }

    @Test
    public void getClientApplicationApiKeyTest() {
        // Setup
        UUID id = UUID.randomUUID();
        String apiKey = "sampleApiKey";
        ClientApplication clientApplicationWithApiKey = ClientApplication.builder()
            .id(id)
            .name("App Name")
            .apiKey(apiKey)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        when(repository.findById(id)).thenReturn(Optional.of(clientApplicationWithApiKey));

        // Execute
        Optional<ApplicationApiKey> result = service.getApiKey(id);

        // Validate
        assertTrue(result.isPresent());
        assertEquals(apiKey, result.get().getApiKey());
    }
}