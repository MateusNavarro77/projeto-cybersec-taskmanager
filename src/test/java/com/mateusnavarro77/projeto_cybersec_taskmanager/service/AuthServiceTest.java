package com.mateusnavarro77.projeto_cybersec_taskmanager.service;

import com.mateusnavarro77.projeto_cybersec_taskmanager.dto.AuthResponseDTO;
import com.mateusnavarro77.projeto_cybersec_taskmanager.dto.LoginRequestDTO;
import com.mateusnavarro77.projeto_cybersec_taskmanager.dto.RegisterRequestDTO;
import com.mateusnavarro77.projeto_cybersec_taskmanager.dto.UserResponseDTO;
import com.mateusnavarro77.projeto_cybersec_taskmanager.entity.User;
import com.mateusnavarro77.projeto_cybersec_taskmanager.repository.UserRepository;
import com.mateusnavarro77.projeto_cybersec_taskmanager.security.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User user;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .role("USER")
                .build();
    }

    @Nested
    @DisplayName("Register Tests")
    class RegisterTests {
        @Test
        @DisplayName("Should register user successfully")
        void shouldRegisterUserSuccessfully() {
            RegisterRequestDTO request = new RegisterRequestDTO("testuser", "test@example.com", "password123");
            given(passwordEncoder.encode(request.password())).willReturn("hashedPassword");
            given(userRepository.save(any(User.class))).willReturn(user);

            UserResponseDTO response = authService.register(request);

            assertThat(response).isNotNull();
            assertThat(response.username()).isEqualTo("testuser");
            assertThat(response.email()).isEqualTo("test@example.com");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowExceptionWhenEmailExists() {
            RegisterRequestDTO request = new RegisterRequestDTO("testuser", "test@example.com", "password123");
            given(userRepository.save(any(User.class))).willThrow(new DataIntegrityViolationException("duplicate"));

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST)
                    .hasMessageContaining("Email already exists");
        }
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {
        @Test
        @DisplayName("Should login successfully and return token")
        void shouldLoginSuccessfully() {
            LoginRequestDTO request = new LoginRequestDTO("test@example.com", "password123");
            Authentication authentication = mock(Authentication.class);
            given(authentication.getPrincipal()).willReturn(user);
            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .willReturn(authentication);
            given(tokenService.generateToken(user)).willReturn("jwt-token");

            AuthResponseDTO response = authService.login(request);

            assertThat(response).isNotNull();
            assertThat(response.token()).isEqualTo("jwt-token");
            assertThat(response.type()).isEqualTo("Bearer");
        }
    }

    @Nested
    @DisplayName("Me Tests")
    class MeTests {
        @Test
        @DisplayName("Should return current user info")
        void shouldReturnCurrentUserInfo() {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);
            given(authentication.getPrincipal()).willReturn(user);
            given(securityContext.getAuthentication()).willReturn(authentication);
            SecurityContextHolder.setContext(securityContext);

            UserResponseDTO response = authService.me();

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(userId);
            assertThat(response.username()).isEqualTo("testuser");
            assertThat(response.email()).isEqualTo("test@example.com");

            SecurityContextHolder.clearContext();
        }
    }
}
