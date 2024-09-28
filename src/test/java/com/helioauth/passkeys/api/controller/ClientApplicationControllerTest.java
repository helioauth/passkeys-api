package com.helioauth.passkeys.api.controller;

import com.helioauth.passkeys.api.service.ClientApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Viktor Stanchev
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClientApplicationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ClientApplicationService clientApplicationService;

    @Test
    void listAll_notAuthorized_whenMissingHeader() throws Exception {
        when(clientApplicationService.listAll()).thenReturn(List.of());

        mockMvc.perform(
            get("/admin/v1/apps")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        ).andExpect(status().isUnauthorized());
    }

    @Test
    void listAll_notAuthorized_whenWrongApiKey() throws Exception {
        when(clientApplicationService.listAll()).thenReturn(List.of());

        mockMvc.perform(
            get("/admin/v1/apps")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("X-Api-Key", "wrongapikey")
        ).andExpect(status().isUnauthorized());
    }

    @Test
    void listAll_ok_whenAuthorized() throws Exception {
        when(clientApplicationService.listAll()).thenReturn(List.of());

        mockMvc.perform(
            get("/admin/v1/apps")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("X-Api-Key", "testapikey")
        ).andExpect(status().isOk());
    }
}