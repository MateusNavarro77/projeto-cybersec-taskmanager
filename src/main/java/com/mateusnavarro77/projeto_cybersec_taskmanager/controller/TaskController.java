package com.mateusnavarro77.projeto_cybersec_taskmanager.controller;

import com.mateusnavarro77.projeto_cybersec_taskmanager.dto.TaskRequestDTO;
import com.mateusnavarro77.projeto_cybersec_taskmanager.dto.TaskResponseDTO;
import com.mateusnavarro77.projeto_cybersec_taskmanager.entity.User;
import com.mateusnavarro77.projeto_cybersec_taskmanager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponseDTO> create(
            @RequestBody @Valid TaskRequestDTO body,
            @AuthenticationPrincipal User user) {
        TaskResponseDTO response = taskService.create(body, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> list(
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) UUID checklistId,
            @AuthenticationPrincipal User user) {
        List<TaskResponseDTO> response = taskService.findAll(user, completed, priority, checklistId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/orphan")
    public ResponseEntity<List<TaskResponseDTO>> listOrphans(@AuthenticationPrincipal User user) {
        List<TaskResponseDTO> response = taskService.findOrphans(user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        TaskResponseDTO response = taskService.findById(id, user);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> update(
            @PathVariable UUID id,
            @RequestBody @Valid TaskRequestDTO body,
            @AuthenticationPrincipal User user) {
        TaskResponseDTO response = taskService.update(id, body, user);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<TaskResponseDTO> complete(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        TaskResponseDTO response = taskService.complete(id, user);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/reopen")
    public ResponseEntity<TaskResponseDTO> reopen(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        TaskResponseDTO response = taskService.reopen(id, user);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        taskService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
