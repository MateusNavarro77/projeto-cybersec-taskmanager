package com.mateusnavarro77.projeto_cybersec_taskmanager.controller;

import com.mateusnavarro77.projeto_cybersec_taskmanager.BaseIT;
import com.mateusnavarro77.projeto_cybersec_taskmanager.dto.TaskRequestDTO;
import com.mateusnavarro77.projeto_cybersec_taskmanager.entity.Checklist;
import com.mateusnavarro77.projeto_cybersec_taskmanager.entity.Task;
import com.mateusnavarro77.projeto_cybersec_taskmanager.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;


import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class TaskControllerIT extends BaseIT {

    @Test
    @DisplayName("Should create task successfully")
    void createTaskSuccess() throws Exception {
        User user = createTestUser("user1", "user1@example.com");
        TaskRequestDTO request = TaskRequestDTO.builder()
                .title("New Task")
                .description("Description")
                .priority("HIGH")
                .build();

        mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", getAuthHeader(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("New Task"))
                .andExpect(jsonPath("$.priority").value("HIGH"));

        assertThat(taskRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should create task with checklist")
    void createTaskWithChecklist() throws Exception {
        User user = createTestUser("user1", "user1@example.com");
        Checklist checklist = checklistRepository.save(Checklist.builder().title("Checklist").user(user).build());
        TaskRequestDTO request = TaskRequestDTO.builder()
                .title("New Task")
                .checklistId(checklist.getId())
                .build();

        mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", getAuthHeader(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.checklistId").value(checklist.getId().toString()));
    }

    @Test
    @DisplayName("Should list only user's tasks with filters")
    void listTasks() throws Exception {
        User user1 = createTestUser("user1", "user1@example.com");
        User user2 = createTestUser("user2", "user2@example.com");

        taskRepository.save(Task.builder().title("User1 Task").user(user1).completed(false).priority("HIGH").build());
        taskRepository.save(Task.builder().title("User2 Task").user(user2).completed(false).priority("HIGH").build());

        mockMvc.perform(get("/api/v1/tasks")
                        .header("Authorization", getAuthHeader(user1))
                        .param("priority", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("User1 Task"));
    }

    @Test
    @DisplayName("Should list orphan tasks")
    void listOrphanTasks() throws Exception {
        User user = createTestUser("user1", "user1@example.com");
        Checklist checklist = checklistRepository.save(Checklist.builder().title("Checklist").user(user).build());

        taskRepository.save(Task.builder().title("Orphan Task").user(user).completed(false).priority("LOW").build());
        taskRepository.save(Task.builder().title("Checklist Task").user(user).checklist(checklist).completed(false).priority("LOW").build());

        mockMvc.perform(get("/api/v1/tasks/orphan")
                        .header("Authorization", getAuthHeader(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Orphan Task"));
    }

    @Test
    @DisplayName("Should get task by id")
    void getTaskById() throws Exception {
        User user = createTestUser("user1", "user1@example.com");
        Task task = taskRepository.save(Task.builder().title("My Task").user(user).completed(false).priority("MEDIUM").build());

        mockMvc.perform(get("/api/v1/tasks/" + task.getId())
                        .header("Authorization", getAuthHeader(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task.getId().toString()));
    }

    @Test
    @DisplayName("Should update task successfully")
    void updateTask() throws Exception {
        User user = createTestUser("user1", "user1@example.com");
        Task task = taskRepository.save(Task.builder().title("Old Task").user(user).completed(false).priority("LOW").build());
        TaskRequestDTO request = TaskRequestDTO.builder().title("Updated Task").build();

        mockMvc.perform(put("/api/v1/tasks/" + task.getId())
                        .header("Authorization", getAuthHeader(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Task"));
    }

    @Test
    @DisplayName("Should complete task")
    void completeTask() throws Exception {
        User user = createTestUser("user1", "user1@example.com");
        Task task = taskRepository.save(Task.builder().title("Task").user(user).completed(false).priority("LOW").build());

        mockMvc.perform(patch("/api/v1/tasks/" + task.getId() + "/complete")
                        .header("Authorization", getAuthHeader(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    @DisplayName("Should reopen task")
    void reopenTask() throws Exception {
        User user = createTestUser("user1", "user1@example.com");
        Task task = taskRepository.save(Task.builder().title("Task").user(user).completed(true).priority("LOW").build());

        mockMvc.perform(patch("/api/v1/tasks/" + task.getId() + "/reopen")
                        .header("Authorization", getAuthHeader(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    @DisplayName("Should delete task")
    void deleteTask() throws Exception {
        User user = createTestUser("user1", "user1@example.com");
        Task task = taskRepository.save(Task.builder().title("To Delete").user(user).completed(false).priority("LOW").build());

        mockMvc.perform(delete("/api/v1/tasks/" + task.getId())
                        .header("Authorization", getAuthHeader(user)))
                .andExpect(status().isNoContent());

        assertThat(taskRepository.existsById(task.getId())).isFalse();
    }

    @Test
    @DisplayName("Should fail validation on invalid payload")
    void createTaskValidationFailure() throws Exception {
        User user = createTestUser("user1", "user1@example.com");
        TaskRequestDTO request = TaskRequestDTO.builder().title("").build(); // Empty title

        mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", getAuthHeader(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 404 when accessing another user's task")
    void accessOtherUserTask() throws Exception {
        User user1 = createTestUser("user1", "user1@example.com");
        User user2 = createTestUser("user2", "user2@example.com");
        Task task = taskRepository.save(Task.builder().title("User1 Task").user(user1).completed(false).priority("LOW").build());

        mockMvc.perform(get("/api/v1/tasks/" + task.getId())
                        .header("Authorization", getAuthHeader(user2)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 404 when adding task to another user's checklist")
    void addTaskToOtherUserChecklist() throws Exception {
        User user1 = createTestUser("user1", "user1@example.com");
        User user2 = createTestUser("user2", "user2@example.com");
        Checklist checklist = checklistRepository.save(Checklist.builder().title("User1 Checklist").user(user1).build());

        TaskRequestDTO request = TaskRequestDTO.builder()
                .title("New Task")
                .checklistId(checklist.getId())
                .build();

        mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", getAuthHeader(user2))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
