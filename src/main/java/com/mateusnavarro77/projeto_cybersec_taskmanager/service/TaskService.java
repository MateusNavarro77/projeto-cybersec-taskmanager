package com.mateusnavarro77.projeto_cybersec_taskmanager.service;

import com.mateusnavarro77.projeto_cybersec_taskmanager.dto.TaskRequestDTO;
import com.mateusnavarro77.projeto_cybersec_taskmanager.dto.TaskResponseDTO;
import com.mateusnavarro77.projeto_cybersec_taskmanager.entity.Checklist;
import com.mateusnavarro77.projeto_cybersec_taskmanager.entity.Task;
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
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ChecklistRepository checklistRepository;

    public TaskResponseDTO create(TaskRequestDTO dto, User user) {
        Checklist checklist = null;
        if (dto.checklistId() != null) {
            checklist = checklistRepository.findByIdAndUser(dto.checklistId(), user)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Checklist not found"));
        }

        Task task = Task.builder()
                .title(dto.title())
                .description(dto.description())
                .completed(false)
                .priority(dto.priority() != null ? dto.priority() : "MEDIUM")
                .dueDate(dto.dueDate())
                .checklist(checklist)
                .user(user)
                .build();

        Task saved = taskRepository.save(task);
        return mapToResponseDTO(saved);
    }

    public List<TaskResponseDTO> findAll(User user, Boolean completed, String priority, UUID checklistId) {
        return taskRepository.findByUserWithFilters(user, completed, priority, checklistId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<TaskResponseDTO> findOrphans(User user) {
        return taskRepository.findByUserAndChecklistIsNullOrderByCreatedAtDesc(user)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public TaskResponseDTO findById(UUID id, User user) {
        Task task = taskRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        return mapToResponseDTO(task);
    }

    public TaskResponseDTO update(UUID id, TaskRequestDTO dto, User user) {
        Task task = taskRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        Checklist checklist = null;
        if (dto.checklistId() != null) {
            checklist = checklistRepository.findByIdAndUser(dto.checklistId(), user)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Checklist not found"));
        }

        task.setTitle(dto.title());
        task.setDescription(dto.description());
        task.setPriority(dto.priority() != null ? dto.priority() : "MEDIUM");
        task.setDueDate(dto.dueDate());
        task.setChecklist(checklist);

        Task updated = taskRepository.save(task);
        return mapToResponseDTO(updated);
    }

    public TaskResponseDTO complete(UUID id, User user) {
        Task task = taskRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        task.setCompleted(true);
        Task updated = taskRepository.save(task);
        return mapToResponseDTO(updated);
    }

    public TaskResponseDTO reopen(UUID id, User user) {
        Task task = taskRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        task.setCompleted(false);
        Task updated = taskRepository.save(task);
        return mapToResponseDTO(updated);
    }

    public void delete(UUID id, User user) {
        Task task = taskRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        taskRepository.delete(task);
    }

    private TaskResponseDTO mapToResponseDTO(Task task) {
        return TaskResponseDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .completed(task.getCompleted())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .checklistId(task.getChecklist() != null ? task.getChecklist().getId() : null)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
