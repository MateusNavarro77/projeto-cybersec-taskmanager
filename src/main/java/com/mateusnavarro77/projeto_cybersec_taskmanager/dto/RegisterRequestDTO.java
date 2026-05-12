package com.mateusnavarro77.projeto_cybersec_taskmanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
    @NotBlank @Size(min = 3, max = 50) String username,
    @NotBlank @Email @Size(max = 120) String email,
    @NotBlank @Size(min = 6, max = 100) String password
) {}
