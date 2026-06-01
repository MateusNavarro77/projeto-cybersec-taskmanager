package com.mateusnavarro77.projeto_cybersec_taskmanager.controller;

import com.mateusnavarro77.projeto_cybersec_taskmanager.BaseIT;
import com.mateusnavarro77.projeto_cybersec_taskmanager.dto.ChecklistRequestDTO;
import com.mateusnavarro77.projeto_cybersec_taskmanager.entity.Checklist;
import com.mateusnavarro77.projeto_cybersec_taskmanager.entity.Task;
import com.mateusnavarro77.projeto_cybersec_taskmanager.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ChecklistControllerIT extends BaseIT {

    @Test
    @DisplayName("Should create checklist successfully")
    void createChecklistSuccess() throws Exception {
        User user = createTestUser("user1", "user1@example.com");
        ChecklistRequestDTO request = new ChecklistRequestDTO("New Checklist", "Description");

        mockMvc.perform(post("/api/v1/checklists")
                        .header("Authorization", getAuthHeader(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("New Checklist"))
                .andExpect(jsonPath("$.description").value("Description"));

        assertThat(checklistRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should list only user's checklists")
    void listChecklists() throws Exception {
        User user1 = createTestUser("user1", "user1@example.com");
        User user2 = createTestUser("user2", "user2@example.com");

        checklistRepository.save(Checklist.builder().title("User1 Checklist").user(user1).build());
        checklistRepository.save(Checklist.builder().title("User2 Checklist").user(user2).build());

        mockMvc.perform(get("/api/v1/checklists")
                        .header("Authorization", getAuthHeader(user1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("User1 Checklist"));
    }

    @Test
    @DisplayName("Should get checklist by id")
    void getChecklistById() throws Exception {
        User user = createTestUser("user1", "user1@example.com");
        Checklist checklist = checklistRepository.save(Checklist.builder().title("My Checklist").user(user).build());

        mockMvc.perform(get("/api/v1/checklists/" + checklist.getId())
                        .header("Authorization", getAuthHeader(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(checklist.getId().toString()))
                .andExpect(jsonPath("$.title").value("My Checklist"));
    }

    @Test
    @DisplayName("Should return 404 when getting non-existent checklist")
    void getChecklistNotFound() throws Exception {
        User user = createTestUser("user1", "user1@example.com");

        mockMvc.perform(get("/api/v1/checklists/" + UUID.randomUUID())
                        .header("Authorization", getAuthHeader(user)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should update checklist successfully")
    void updateChecklist() throws Exception {
        User user = createTestUser("user1", "user1@example.com");
        Checklist checklist = checklistRepository.save(Checklist.builder().title("Old Title").user(user).build());
        ChecklistRequestDTO request = new ChecklistRequestDTO("New Title", "New Description");

        mockMvc.perform(put("/api/v1/checklists/" + checklist.getId())
                        .header("Authorization", getAuthHeader(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"))
                .andExpect(jsonPath("$.description").value("New Description"));

        Checklist updated = checklistRepository.findById(checklist.getId()).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("New Title");
    }

    @Test
    @DisplayName("Should delete checklist successfully")
    void deleteChecklist() throws Exception {
        User user = createTestUser("user1", "user1@example.com");
        Checklist checklist = checklistRepository.save(Checklist.builder().title("To Delete").user(user).build());

        mockMvc.perform(delete("/api/v1/checklists/" + checklist.getId())
                        .header("Authorization", getAuthHeader(user)))
                .andExpect(status().isNoContent());

        assertThat(checklistRepository.existsById(checklist.getId())).isFalse();
    }

    @Test
    @DisplayName("Should list tasks of checklist")
    void listTasksOfChecklist() throws Exception {
        User user = createTestUser("user1", "user1@example.com");
        Checklist checklist = checklistRepository.save(Checklist.builder().title("My Checklist").user(user).build());
        taskRepository.save(Task.builder().title("Task 1").user(user).checklist(checklist).completed(false).priority("LOW").build());

        mockMvc.perform(get("/api/v1/checklists/" + checklist.getId() + "/tasks")
                        .header("Authorization", getAuthHeader(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Task 1"));
    }

    @Test
    @DisplayName("Should fail validation on invalid payload")
    void createChecklistValidationFailure() throws Exception {
        User user = createTestUser("user1", "user1@example.com");
        ChecklistRequestDTO request = new ChecklistRequestDTO("", "Description"); // Empty title

        mockMvc.perform(post("/api/v1/checklists")
                        .header("Authorization", getAuthHeader(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 403 when accessing another user's checklist")
    void accessOtherUserChecklist() throws Exception {
        User user1 = createTestUser("user1", "user1@example.com");
        User user2 = createTestUser("user2", "user2@example.com");
        Checklist checklist = checklistRepository.save(Checklist.builder().title("User1 Checklist").user(user1).build());

        mockMvc.perform(get("/api/v1/checklists/" + checklist.getId())
                        .header("Authorization", getAuthHeader(user2)))
                .andExpect(status().isNotFound()); // Service returns 404 for findByIdAndUser
    }
}
