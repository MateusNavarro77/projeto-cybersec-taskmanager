package com.mateusnavarro77.projeto_cybersec_taskmanager.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/debug")
public class DebugController {

    @GetMapping("/error")
    public void triggerError() {
        throw new RuntimeException("Endpoint que lança uma exceção para testar o GlobalExceptionHandler.");
    }
}
