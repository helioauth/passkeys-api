package com.helioauth.passkeys.api.service;

import com.helioauth.passkeys.api.domain.ClientApplication;
import com.helioauth.passkeys.api.domain.ClientApplicationRepository;
import com.helioauth.passkeys.api.mapper.ClientApplicationMapper;
import com.helioauth.passkeys.api.service.dto.ClientApplicationApiKeyDTO;
import com.helioauth.passkeys.api.service.dto.ClientApplicationDTO;
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

    public static final ClientApplicationDTO DTO = new ClientApplicationDTO(
        UUID.randomUUID(),
        "App Name",
        Instant.now(),
        Instant.now()
    );

    public static final ClientApplication CLIENT_APPLICATION = ClientApplication.builder()
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
        ArgumentCaptor<ClientApplication> argumentCaptor = ArgumentCaptor.forClass(ClientApplication.class);
        when(repository.save(any(ClientApplication.class))).thenReturn(CLIENT_APPLICATION);

        // Execute
        ClientApplicationDTO result = service.add(DTO.name());

        // Capture the argument
        verify(repository).save(argumentCaptor.capture());
        ClientApplication savedClientApplication = argumentCaptor.getValue();

        // Validate
        assertFalse(savedClientApplication.getApiKey().isEmpty());
        assertEquals(DTO.name(), savedClientApplication.getName());
        assertEquals(DTO.name(), result.name());
    }

    @Test
    public void listAllClientApplicationsTest() {
        // Setup
        List<ClientApplication> applications = List.of(CLIENT_APPLICATION);
        when(repository.findAll()).thenReturn(applications);

        // Execute
        List<ClientApplicationDTO> result = service.listAll();

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
        ClientApplication existingClientApplication = ClientApplication.builder()
            .id(id)
            .name("Old Name")
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        when(repository.findById(id)).thenReturn(Optional.of(existingClientApplication));
        when(repository.save(any(ClientApplication.class))).thenReturn(existingClientApplication);

        // Execute
        Optional<ClientApplicationDTO> result = service.edit(id, newName);

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
        Optional<ClientApplicationDTO> result = service.get(id);

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
        ClientApplication clientApplicationWithApiKey = ClientApplication.builder()
            .id(id)
            .name("App Name")
            .apiKey(apiKey)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

        when(repository.findById(id)).thenReturn(Optional.of(clientApplicationWithApiKey));

        // Execute
        Optional<ClientApplicationApiKeyDTO> result = service.getApiKey(id);

        // Validate
        assertTrue(result.isPresent());
        assertEquals(apiKey, result.get().apiKey());
    }
}