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

import com.helioauth.passkeys.api.contract.SignUpStartRequest;
import com.helioauth.passkeys.api.contract.SignInStartRequest;
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
        SignUpStartRequest request = new SignUpStartRequest("test");

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/v1/credentials/signup/start")
                .contentType("application/json")
                .content(requestJson)
            ).andExpect(status().isOk());
    }
    
    @Test
    void postSignInStart() throws Exception {
        SignInStartRequest request = new SignInStartRequest("test");

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/v1/credentials/signin/start")
            .contentType("application/json")
            .content(requestJson)
        ).andExpect(status().isOk());
    }
}