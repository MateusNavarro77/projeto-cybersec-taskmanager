package com.mateusnavarro77.projeto_cybersec_taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record TaskRequestDTO(
    @NotBlank(message = "Title is required")
    @Size(max = 120, message = "Title must be at most 120 characters")
    String title,

    String description,

    String priority,

    LocalDateTime dueDate,

    UUID checklistId
) {}
