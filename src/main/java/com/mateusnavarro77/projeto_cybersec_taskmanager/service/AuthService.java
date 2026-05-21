package com.mateusnavarro77.projeto_cybersec_taskmanager.service;

import com.mateusnavarro77.projeto_cybersec_taskmanager.dto.AuthResponseDTO;
import com.mateusnavarro77.projeto_cybersec_taskmanager.dto.LoginRequestDTO;
import com.mateusnavarro77.projeto_cybersec_taskmanager.dto.RegisterRequestDTO;
import com.mateusnavarro77.projeto_cybersec_taskmanager.dto.UserResponseDTO;
import com.mateusnavarro77.projeto_cybersec_taskmanager.entity.User;
import com.mateusnavarro77.projeto_cybersec_taskmanager.repository.UserRepository;
import com.mateusnavarro77.projeto_cybersec_taskmanager.security.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private AuthenticationManager authenticationManager;

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
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email already exists");
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
