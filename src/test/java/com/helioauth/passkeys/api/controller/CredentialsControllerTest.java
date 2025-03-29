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

import com.helioauth.passkeys.api.domain.ClientApplication;
import com.helioauth.passkeys.api.domain.ClientApplicationRepository;
import com.helioauth.passkeys.api.domain.User;
import com.helioauth.passkeys.api.domain.UserCredentialRepository;
import com.helioauth.passkeys.api.domain.UserRepository;
import com.helioauth.passkeys.api.generated.models.SignInStartRequest;
import com.helioauth.passkeys.api.generated.models.SignUpFinishRequest;
import com.helioauth.passkeys.api.generated.models.SignUpStartRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class CredentialsControllerTest {
    public static final String X_APP_ID = "X-App-Id";
    public static final String X_API_KEY = "X-Api-Key";

    public static final String PATH_SIGNUP_START = "/v1/signup/start";
    public static final String PATH_SIGNUP_FINISH = "/v1/signup/finish";

    public static final String PATH_SIGNIN_START = "/v1/signin/start";
    public static final String PATH_SIGNIN_FINISH = "/v1/signin/finish";

    public static final UUID TEST_APP_ID = UUID.randomUUID();
    public static final ClientApplication TEST_APP = ClientApplication.builder()
        .id(TEST_APP_ID)
        .name("test")
        .apiKey("testapikey")
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .build();

    @Container
    static final PostgreSQLContainer<?> postgresql = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        postgresql.start();
        registry.add("spring.datasource.url", postgresql::getJdbcUrl);
        registry.add("spring.datasource.username", postgresql::getUsername);
        registry.add("spring.datasource.password", postgresql::getPassword);
    }

    @MockBean
    ClientApplicationRepository clientApplicationRepository;
    
    @Autowired
    UserRepository userRepository;
    
    @Autowired
    UserCredentialRepository userCredentialRepository;

    @Autowired
    MockMvc mockMvc;
    
    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        when(clientApplicationRepository.findById(TEST_APP_ID))
            .thenReturn(Optional.of(TEST_APP));

        when(clientApplicationRepository.findByApiKey(TEST_APP.getApiKey()))
            .thenReturn(Optional.of(TEST_APP));
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        userCredentialRepository.deleteAll();
    }

    @Test
    void postSignUpStart() throws Exception {
        SignUpStartRequest request = new SignUpStartRequest("test");

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post(PATH_SIGNUP_START)
                .header(X_APP_ID, TEST_APP_ID.toString())
                .contentType("application/json")
                .content(requestJson)
            ).andExpect(status().isOk());
    }

    @Test
    void postSignUpStart_existingUser() throws Exception {
        userRepository.save(User.builder()
            .name("test")
            .displayName("test")
            .build());

        SignUpStartRequest request = new SignUpStartRequest("test");

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post(PATH_SIGNUP_START)
            .header(X_APP_ID, TEST_APP_ID.toString())
            .contentType("application/json")
            .content(requestJson)
        ).andExpect(status().isOk());
    }

    @Test
    void postSignUpStart_nonExistingApp() throws Exception {
        mockMvc.perform(post(PATH_SIGNUP_START)
            .header(X_APP_ID, UUID.randomUUID().toString())
            .contentType("application/json")
            .content("{}")
        ).andExpect(status().isUnauthorized());
    }

    @Test
    void postSignUpFinish() throws Exception {
        SignUpFinishRequest request = new SignUpFinishRequest("requestId", "publicKeyCredential");
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post(PATH_SIGNUP_FINISH)
            .header(X_API_KEY, TEST_APP.getApiKey())
            .contentType("application/json")
            .content(requestJson)
        ).andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    void postSignUpFinish_nonExistingApp() throws Exception {
        mockMvc.perform(post(PATH_SIGNUP_FINISH)
            .header(X_API_KEY, "invalid api key")
            .contentType("application/json")
            .content("{}")
        ).andExpect(status().isUnauthorized());
    }

    @Test
    void postSignInStart() throws Exception {
        SignInStartRequest request = new SignInStartRequest("test");

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post(PATH_SIGNIN_START)
            .contentType("application/json")
            .content(requestJson)
        ).andExpect(status().isOk());
    }
}