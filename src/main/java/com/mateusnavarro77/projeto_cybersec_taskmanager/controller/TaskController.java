package com.mateusnavarro77.projeto_cybersec_taskmanager.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) UUID checklistId) {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/orphan")
    public ResponseEntity<?> listOrphans() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<?> complete(@PathVariable UUID id) {
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/reopen")
    public ResponseEntity<?> reopen(@PathVariable UUID id) {
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        return ResponseEntity.noContent().build();
    }
}
