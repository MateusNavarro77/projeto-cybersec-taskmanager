package com.mateusnavarro77.projeto_cybersec_taskmanager.dto;

import java.util.UUID;

public record UserResponseDTO(
    UUID id,
    String username,
    String email
) {}
