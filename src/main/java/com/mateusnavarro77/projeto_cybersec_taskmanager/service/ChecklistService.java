package com.mateusnavarro77.projeto_cybersec_taskmanager.service;

import com.mateusnavarro77.projeto_cybersec_taskmanager.dto.ChecklistRequestDTO;
import com.mateusnavarro77.projeto_cybersec_taskmanager.dto.ChecklistResponseDTO;
import com.mateusnavarro77.projeto_cybersec_taskmanager.entity.Checklist;
import com.mateusnavarro77.projeto_cybersec_taskmanager.entity.User;
import com.mateusnavarro77.projeto_cybersec_taskmanager.repository.ChecklistRepository;
import com.mateusnavarro77.projeto_cybersec_taskmanager.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ChecklistService {

    @Autowired
    private ChecklistRepository checklistRepository;

    @Autowired
    private TaskRepository taskRepository;

    public ChecklistResponseDTO create(ChecklistRequestDTO dto, User user) {
        Checklist checklist = Checklist.builder()
                .title(dto.title())
                .description(dto.description())
                .user(user)
                .build();
        
        Checklist saved = checklistRepository.save(checklist);
        return mapToResponseDTO(saved);
    }

    public List<ChecklistResponseDTO> findAll(User user) {
        return checklistRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public ChecklistResponseDTO findById(UUID id, User user) {
        Checklist checklist = checklistRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Checklist not found"));
        return mapToResponseDTO(checklist);
    }

    public ChecklistResponseDTO update(UUID id, ChecklistRequestDTO dto, User user) {
        Checklist checklist = checklistRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Checklist not found"));
        
        checklist.setTitle(dto.title());
        checklist.setDescription(dto.description());
        
        Checklist updated = checklistRepository.save(checklist);
        return mapToResponseDTO(updated);
    }

    public void delete(UUID id, User user) {
        Checklist checklist = checklistRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Checklist not found"));
        checklistRepository.delete(checklist);
    }

    public List<?> listTasks(UUID id, User user) {
        Checklist checklist = checklistRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Checklist not found"));
        
        // Return tasks associated with this checklist. 
        // Note: TaskResponseDTO isn't created yet, so returning raw entities for now 
        // or a simple map if preferred. Let's return the entities which will be serialized to JSON.
        return taskRepository.findByChecklistAndUserOrderByCreatedAtDesc(checklist, user);
    }

    private ChecklistResponseDTO mapToResponseDTO(Checklist checklist) {
        return ChecklistResponseDTO.builder()
                .id(checklist.getId())
                .title(checklist.getTitle())
                .description(checklist.getDescription())
                .createdAt(checklist.getCreatedAt())
                .updatedAt(checklist.getUpdatedAt())
                .build();
    }
}
