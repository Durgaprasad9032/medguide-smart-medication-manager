package com.medguide.modules.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medguide.modules.auth.dto.LoginRequest;
import com.medguide.modules.auth.dto.RefreshTokenRequest;
import com.medguide.modules.auth.dto.RegisterRequest;
import com.medguide.modules.user.domain.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Complete authentication flow: register -> login -> get me -> refresh -> logout")
    void shouldCompleteAuthenticationFlow() throws Exception {
        String email = "flow_" + UUID.randomUUID() + "@medguide.local";
        String password = "StrongPassword@123";

        // 1. Registration
        RegisterRequest registerRequest = new RegisterRequest(email, password, Role.PATIENT, "en");
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.role").value("PATIENT"));

        // 2. Duplicate registration attempt must fail with 409 Conflict
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));

        // 3. Login
        LoginRequest loginRequest = new LoginRequest(email, password);
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.refreshToken").isString())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(responseJson).get("data").get("accessToken").asText();
        String refreshToken = objectMapper.readTree(responseJson).get("data").get("refreshToken").asText();

        // 4. Access protected endpoint GET /api/v1/auth/me with Bearer token
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(email));

        // 5. Access protected endpoint without Bearer token must fail with 401 Unauthorized
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));

        // 6. Refresh token rotation
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest(refreshToken);
        MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.refreshToken").isString())
                .andReturn();

        String refreshResponseJson = refreshResult.getResponse().getContentAsString();
        String rotatedRefreshToken = objectMapper.readTree(refreshResponseJson).get("data").get("refreshToken").asText();

        // 7. Logout with the active rotated refresh token
        RefreshTokenRequest logoutRequest = new RefreshTokenRequest(rotatedRefreshToken);
        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Public registration as ADMIN is strictly blocked with 403 Forbidden")
    void shouldBlockPublicAdminRegistration() throws Exception {
        RegisterRequest adminRequest = new RegisterRequest("admin_" + UUID.randomUUID() + "@medguide.local", "StrongPass@123", Role.ADMIN, "en");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("Login with incorrect password returns 401 Unauthorized with generic message")
    void shouldFailLoginWithIncorrectPassword() throws Exception {
        String email = "wrongpass_" + UUID.randomUUID() + "@medguide.local";
        authServiceRegister(email, "CorrectPass@123", Role.PATIENT);

        LoginRequest badLogin = new LoginRequest(email, "WrongPassword@123");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badLogin)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    private void authServiceRegister(String email, String password, Role role) throws Exception {
        RegisterRequest request = new RegisterRequest(email, password, role, "en");
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }
}
