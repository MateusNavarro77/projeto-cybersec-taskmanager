package com.mateusnavarro77.projeto_cybersec_taskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mateusnavarro77.projeto_cybersec_taskmanager.TestcontainersConfiguration;
import com.mateusnavarro77.projeto_cybersec_taskmanager.dto.LoginRequestDTO;
import com.mateusnavarro77.projeto_cybersec_taskmanager.dto.RegisterRequestDTO;
import com.mateusnavarro77.projeto_cybersec_taskmanager.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("integrationtest")
public class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Ensure db is clean before each test
        userRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("POST /api/v1/auth/register")
    class RegisterTests {

        @Test
        @DisplayName("Should register a new user successfully")
        void registerSuccess() throws Exception {
            RegisterRequestDTO request = new RegisterRequestDTO("integrationuser", "integration@example.com", "Password123!");

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.username").value("integrationuser"))
                    .andExpect(jsonPath("$.email").value("integration@example.com"));

            // Verify in DB
            assertThat(userRepository.findByEmail("integration@example.com")).isPresent();
        }

        @Test
        @DisplayName("Should fail when email already exists")
        void registerDuplicateEmail() throws Exception {
            RegisterRequestDTO request = new RegisterRequestDTO("integrationuser", "integration@example.com", "Password123!");

            // Register first time
            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            // Try to register again with same email
            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should fail validation on invalid payload")
        void registerValidationFailure() throws Exception {
            // Invalid email and too short password
            RegisterRequestDTO request = new RegisterRequestDTO("user", "not-an-email", "123");

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
            
            // Verify no user was created
            assertThat(userRepository.count()).isZero();
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/login")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully and return JWT")
        void loginSuccess() throws Exception {
            // Pre-register user
            RegisterRequestDTO registerRequest = new RegisterRequestDTO("loginuser", "login@example.com", "Password123!");
            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isCreated());

            // Attempt Login
            LoginRequestDTO loginRequest = new LoginRequestDTO("login@example.com", "Password123!");
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").exists())
                    .andExpect(jsonPath("$.type").value("Bearer"));
        }

        @Test
        @DisplayName("Should fail login with wrong password")
        void loginWrongPassword() throws Exception {
            // Pre-register user
            RegisterRequestDTO registerRequest = new RegisterRequestDTO("loginuser", "login@example.com", "Password123!");
            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isCreated());

            // Attempt Login
            LoginRequestDTO loginRequest = new LoginRequestDTO("login@example.com", "WrongPassword!");
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isForbidden()); // Spring security typically returns 403 or 401 for bad credentials
        }

        @Test
        @DisplayName("Should fail login for non-existent user")
        void loginUserNotFound() throws Exception {
            LoginRequestDTO loginRequest = new LoginRequestDTO("notfound@example.com", "Password123!");
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/auth/me")
    class MeTests {

        @Test
        @DisplayName("Should retrieve current user info with valid token")
        void getMeSuccess() throws Exception {
            // 1. Register
            RegisterRequestDTO registerRequest = new RegisterRequestDTO("meuser", "me@example.com", "Password123!");
            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isCreated());

            // 2. Login to get token
            LoginRequestDTO loginRequest = new LoginRequestDTO("me@example.com", "Password123!");
            MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andReturn();

            String responseBody = loginResult.getResponse().getContentAsString();
            String token = objectMapper.readTree(responseBody).get("token").asText();

            // 3. Hit /me endpoint
            mockMvc.perform(get("/api/v1/auth/me")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("meuser"))
                    .andExpect(jsonPath("$.email").value("me@example.com"));
        }

        @Test
        @DisplayName("Should fail to retrieve info without token")
        void getMeNoToken() throws Exception {
            mockMvc.perform(get("/api/v1/auth/me"))
                    .andExpect(status().isForbidden()); // Or 401 Unauthorized
        }

        @Test
        @DisplayName("Should fail to retrieve info with invalid token")
        void getMeInvalidToken() throws Exception {
            mockMvc.perform(get("/api/v1/auth/me")
                            .header("Authorization", "Bearer invalid.token.here"))
                    .andExpect(status().isForbidden());
        }
    }
}
