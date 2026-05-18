package com.mateusnavarro77.projeto_cybersec_taskmanager.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ChecklistResponseDTO(
    UUID id,
    String title,
    String description,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
