package com.ticketing.domain.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
class AuthFlowIT {

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;

  @Test
  void signupThenLoginThenAccessProtectedEndpoint() throws Exception {
    String email = "flow-" + System.nanoTime() + "@example.com";

    mockMvc
        .perform(
            post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"email":"%s","password":"password1","nickname":"flow"}
                    """
                        .formatted(email)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.email").value(email));

    String loginResponse =
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"email":"%s","password":"password1"}
                        """
                            .formatted(email)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    JsonNode data = objectMapper.readTree(loginResponse).path("data");
    String accessToken = data.path("accessToken").asText();
    assertThat(accessToken).isNotBlank();

    mockMvc
        .perform(get("/api/v1/members/me").header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.email").value(email))
        .andExpect(jsonPath("$.data.role").value("USER"));
  }

  @Test
  void protectedEndpointWithoutTokenReturns401Envelope() throws Exception {
    mockMvc
        .perform(get("/api/v1/members/me"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("AUTH_REQUIRED"));
  }

  @Test
  void invalidTokenReturns401Envelope() throws Exception {
    mockMvc
        .perform(get("/api/v1/members/me").header("Authorization", "Bearer not.a.jwt"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error.code").value("INVALID_TOKEN"));
  }
}
