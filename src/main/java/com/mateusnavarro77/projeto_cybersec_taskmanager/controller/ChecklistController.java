package com.mateusnavarro77.projeto_cybersec_taskmanager.controller;

import com.mateusnavarro77.projeto_cybersec_taskmanager.dto.ChecklistRequestDTO;
import com.mateusnavarro77.projeto_cybersec_taskmanager.dto.ChecklistResponseDTO;
import com.mateusnavarro77.projeto_cybersec_taskmanager.entity.User;
import com.mateusnavarro77.projeto_cybersec_taskmanager.service.ChecklistService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/checklists")
public class ChecklistController {

    @Autowired
    private ChecklistService checklistService;

    @PostMapping
    public ResponseEntity<ChecklistResponseDTO> create(
            @RequestBody @Valid ChecklistRequestDTO body,
            @AuthenticationPrincipal User user) {
        ChecklistResponseDTO response = checklistService.create(body, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ChecklistResponseDTO>> list(@AuthenticationPrincipal User user) {
        List<ChecklistResponseDTO> response = checklistService.findAll(user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChecklistResponseDTO> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        ChecklistResponseDTO response = checklistService.findById(id, user);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChecklistResponseDTO> update(
            @PathVariable UUID id,
            @RequestBody @Valid ChecklistRequestDTO body,
            @AuthenticationPrincipal User user) {
        ChecklistResponseDTO response = checklistService.update(id, body, user);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        checklistService.delete(id, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/tasks")
    public ResponseEntity<List<?>> listTasks(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        List<?> response = checklistService.listTasks(id, user);
        return ResponseEntity.ok(response);
    }
}
