package com.helioauth.passkeys.api.controller;

import com.helioauth.passkeys.api.contract.CreateCredentialRequest;
import com.helioauth.passkeys.api.contract.StartAssertionRequest;
import com.helioauth.passkeys.api.domain.UserCredentialRepository;
import com.helioauth.passkeys.api.domain.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class CredentialsControllerTest {

    @Container
    static final PostgreSQLContainer<?> postgresql = new PostgreSQLContainer<>("postgres:15");
    
    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        postgresql.start();
        registry.add("spring.datasource.url", postgresql::getJdbcUrl);
        registry.add("spring.datasource.username", postgresql::getUsername);
        registry.add("spring.datasource.password", postgresql::getPassword);
    }
    
    @Autowired
    UserRepository userRepository;
    
    @Autowired
    UserCredentialRepository userCredentialRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        userCredentialRepository.deleteAll();
    }
    
    @Autowired
    MockMvc mockMvc;
    
    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void postSignUpStart() throws Exception {
        CreateCredentialRequest request = new CreateCredentialRequest("test");

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/v1/credentials/signup/start")
                .contentType("application/json")
                .content(requestJson)
            ).andExpect(status().isOk());
    }
    
    @Test
    void postSignInStart() throws Exception {
        StartAssertionRequest request = new StartAssertionRequest("test");

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/v1/credentials/signin/start")
            .contentType("application/json")
            .content(requestJson)
        ).andExpect(status().isOk());
    }
}