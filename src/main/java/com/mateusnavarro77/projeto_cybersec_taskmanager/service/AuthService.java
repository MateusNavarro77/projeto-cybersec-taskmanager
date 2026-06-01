package com.mateusnavarro77.projeto_cybersec_taskmanager.service;

import com.mateusnavarro77.projeto_cybersec_taskmanager.dto.AuthResponseDTO;
import com.mateusnavarro77.projeto_cybersec_taskmanager.dto.LoginRequestDTO;
import com.mateusnavarro77.projeto_cybersec_taskmanager.dto.RegisterRequestDTO;
import com.mateusnavarro77.projeto_cybersec_taskmanager.dto.UserResponseDTO;
import com.mateusnavarro77.projeto_cybersec_taskmanager.entity.User;
import com.mateusnavarro77.projeto_cybersec_taskmanager.exception.BusinessException;
import com.mateusnavarro77.projeto_cybersec_taskmanager.repository.UserRepository;
import com.mateusnavarro77.projeto_cybersec_taskmanager.security.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final TokenService tokenService;

    private final AuthenticationManager authenticationManager;

    public UserResponseDTO register(RegisterRequestDTO data) {
        String encryptedPassword = passwordEncoder.encode(data.password());

        User newUser = User.builder()
                .username(data.username())
                .email(data.email())
                .passwordHash(encryptedPassword)
                .role("USER")
                .build();

        try {
            User savedUser = userRepository.save(newUser);

            return UserResponseDTO.builder()
                    .id(savedUser.getId())
                    .username(savedUser.getRealUsername())
                    .email(savedUser.getEmail())
                    .build();

        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("Email already exists");
        }
    }

    public AuthResponseDTO login(LoginRequestDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        var token = tokenService.generateToken((User) auth.getPrincipal());

        return AuthResponseDTO.builder()
                .token(token)
                .type("Bearer")
                .build();
    }

    public UserResponseDTO me() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getRealUsername())
                .email(user.getEmail())
                .build();
    }
}
