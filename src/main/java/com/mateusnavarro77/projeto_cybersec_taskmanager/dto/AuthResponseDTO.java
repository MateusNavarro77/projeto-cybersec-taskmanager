package com.mateusnavarro77.projeto_cybersec_taskmanager.dto;

import lombok.Builder;

@Builder
public record AuthResponseDTO(
    String token,
    String type
) {}
