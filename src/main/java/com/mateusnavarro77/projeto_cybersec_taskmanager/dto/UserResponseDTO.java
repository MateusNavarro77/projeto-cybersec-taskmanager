package com.mateusnavarro77.projeto_cybersec_taskmanager.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UserResponseDTO(
    UUID id,
    String username,
    String email
) {}
