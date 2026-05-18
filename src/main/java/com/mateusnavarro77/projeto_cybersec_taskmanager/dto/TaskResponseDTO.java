package com.mateusnavarro77.projeto_cybersec_taskmanager.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record TaskResponseDTO(
    UUID id,
    String title,
    String description,
    Boolean completed,
    String priority,
    LocalDateTime dueDate,
    UUID checklistId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
