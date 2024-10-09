package com.helioauth.passkeys.api.service;

import com.helioauth.passkeys.api.domain.ClientApplicationRepository;
import com.helioauth.passkeys.api.mapper.ClientApplicationMapper;
import com.helioauth.passkeys.api.service.dto.ClientApplication;
import com.helioauth.passkeys.api.service.dto.ClientApplicationApiKey;
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

    public static final ClientApplication DTO = new ClientApplication(
        UUID.randomUUID(),
        "App Name",
        Instant.now(),
        Instant.now()
    );

    public static final com.helioauth.passkeys.api.domain.ClientApplication CLIENT_APPLICATION = com.helioauth.passkeys.api.domain.ClientApplication.builder()
        .id(DTO.id())
        .name(DTO.name())
        .createdAt(DTO.createdAt())
        .updatedAt(DTO.updatedAt())
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
        ArgumentCaptor<com.helioauth.passkeys.api.domain.ClientApplication> argumentCaptor = ArgumentCaptor.forClass(com.helioauth.passkeys.api.domain.ClientApplication.class);
        when(repository.save(any(com.helioauth.passkeys.api.domain.ClientApplication.class))).thenReturn(CLIENT_APPLICATION);

        // Execute
        ClientApplication result = service.add(DTO.name());

        // Capture the argument
        verify(repository).save(argumentCaptor.capture());
        com.helioauth.passkeys.api.domain.ClientApplication savedClientApplication = argumentCaptor.getValue();

        // Validate
        assertFalse(savedClientApplication.getApiKey().isEmpty());
        assertEquals(DTO.name(), savedClientApplication.getName());
        assertEquals(DTO.name(), result.name());
    }

    @Test
    public void listAllClientApplicationsTest() {
        // Setup
        List<com.helioauth.passkeys.api.domain.ClientApplication> applications = List.of(CLIENT_APPLICATION);
        when(repository.findAll()).thenReturn(applications);

        // Execute
        List<ClientApplication> result = service.listAll();

        // Validate
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(DTO.id(), result.getFirst().id());
        assertEquals(DTO.name(), result.getFirst().name());
    }

    @Test
    public void editClientApplicationTest() {
        // Setup
        UUID id = UUID.randomUUID();
        String newName = "Updated Name";
        com.helioauth.passkeys.api.domain.ClientApplication existingClientApplication = com.helioauth.passkeys.api.domain.ClientApplication.builder()
            .id(id)
            .name("Old Name")
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        when(repository.findById(id)).thenReturn(Optional.of(existingClientApplication));
        when(repository.save(any(com.helioauth.passkeys.api.domain.ClientApplication.class))).thenReturn(existingClientApplication);

        // Execute
        Optional<ClientApplication> result = service.edit(id, newName);

        // Validate
        assertTrue(result.isPresent());
        assertEquals(newName, result.get().name());
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
        UUID id = DTO.id();
        when(repository.findById(id)).thenReturn(Optional.of(CLIENT_APPLICATION));

        // Execute
        Optional<ClientApplication> result = service.get(id);

        // Validate
        assertTrue(result.isPresent());
        assertEquals(DTO.id(), result.get().id());
        assertEquals(DTO.name(), result.get().name());
    }

    @Test
    public void getClientApplicationApiKeyTest() {
        // Setup
        UUID id = UUID.randomUUID();
        String apiKey = "sampleApiKey";
        com.helioauth.passkeys.api.domain.ClientApplication clientApplicationWithApiKey = com.helioauth.passkeys.api.domain.ClientApplication.builder()
            .id(id)
            .name("App Name")
            .apiKey(apiKey)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        when(repository.findById(id)).thenReturn(Optional.of(clientApplicationWithApiKey));

        // Execute
        Optional<ClientApplicationApiKey> result = service.getApiKey(id);

        // Validate
        assertTrue(result.isPresent());
        assertEquals(apiKey, result.get().apiKey());
    }
}