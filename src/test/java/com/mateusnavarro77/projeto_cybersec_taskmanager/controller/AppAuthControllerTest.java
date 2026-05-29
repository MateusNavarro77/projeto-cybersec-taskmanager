package com.mateusnavarro77.projeto_cybersec_taskmanager.controller;

import com.mateusnavarro77.projeto_cybersec_taskmanager.repository.UserRepository;
import com.mateusnavarro77.projeto_cybersec_taskmanager.security.SecurityFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("unittest")
class AppAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /app/login should render login form")
    void loginForm_PublicRequest_RendersLoginPage() throws Exception {
        mockMvc.perform(get("/app/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("app/auth/login"))
                .andExpect(model().attributeExists("loginRequest"));
    }

    @Test
    @DisplayName("GET /app/register should render register form")
    void registerForm_PublicRequest_RendersRegisterPage() throws Exception {
        mockMvc.perform(get("/app/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("app/auth/register"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    @Test
    @DisplayName("POST /app/register should validate form input")
    void register_InvalidForm_RendersRegisterPageWithErrors() throws Exception {
        mockMvc.perform(post("/app/register")
                        .with(csrf())
                        .param("username", "ab")
                        .param("email", "not-an-email")
                        .param("password", "123"))
                .andExpect(status().isOk())
                .andExpect(view().name("app/auth/register"))
                .andExpect(model().attributeHasFieldErrors("registerRequest", "username", "email", "password"));

        assertThat(userRepository.count()).isZero();
    }

    @Test
    @DisplayName("POST /app/register should create user and redirect to login")
    void register_ValidForm_CreatesUserAndRedirectsToLogin() throws Exception {
        mockMvc.perform(post("/app/register")
                        .with(csrf())
                        .param("username", "webuser")
                        .param("email", "web@example.com")
                        .param("password", "Password123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/app/login"));

        assertThat(userRepository.findByEmail("web@example.com")).isPresent();
    }

    @Test
    @DisplayName("POST /app/login should set JWT cookie and redirect to app")
    void login_ValidCredentials_SetsAuthCookieAndRedirectsToApp() throws Exception {
        mockMvc.perform(post("/app/register")
                        .with(csrf())
                        .param("username", "loginuser")
                        .param("email", "login-web@example.com")
                        .param("password", "Password123!"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/app/login")
                        .with(csrf())
                        .param("email", "login-web@example.com")
                        .param("password", "Password123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/app"))
                .andExpect(cookie().exists(SecurityFilter.AUTH_COOKIE_NAME))
                .andExpect(result -> assertThat(result.getResponse().getHeader(HttpHeaders.SET_COOKIE))
                        .contains("HttpOnly")
                        .contains("SameSite=Lax"));
    }

    @Test
    @DisplayName("GET /app should redirect anonymous users to login")
    void appHome_AnonymousUser_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/app"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/app/login"));
    }
}
