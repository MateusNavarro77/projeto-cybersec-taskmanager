package com.mateusnavarro77.projeto_cybersec_taskmanager.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
    int status,
    String message,
    LocalDateTime timestamp,
    List<FieldError> errors
) {
    public ErrorResponse(int status, String message) {
        this(status, message, LocalDateTime.now(), null);
    }

    public ErrorResponse(int status, String message, List<FieldError> errors) {
        this(status, message, LocalDateTime.now(), errors);
    }
}
