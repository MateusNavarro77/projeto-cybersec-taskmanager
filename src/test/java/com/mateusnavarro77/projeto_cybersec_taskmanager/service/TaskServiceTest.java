package com.mateusnavarro77.projeto_cybersec_taskmanager.service;

import com.mateusnavarro77.projeto_cybersec_taskmanager.dto.TaskRequestDTO;
import com.mateusnavarro77.projeto_cybersec_taskmanager.dto.TaskResponseDTO;
import com.mateusnavarro77.projeto_cybersec_taskmanager.entity.Checklist;
import com.mateusnavarro77.projeto_cybersec_taskmanager.entity.Task;
import com.mateusnavarro77.projeto_cybersec_taskmanager.entity.User;
import com.mateusnavarro77.projeto_cybersec_taskmanager.repository.ChecklistRepository;
import com.mateusnavarro77.projeto_cybersec_taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ChecklistRepository checklistRepository;

    @InjectMocks
    private TaskService taskService;

    private User user;
    private Checklist checklist;
    private Task task;
    private final UUID userId = UUID.randomUUID();
    private final UUID checklistId = UUID.randomUUID();
    private final UUID taskId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(userId)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashed")
                .role("USER")
                .build();

        checklist = Checklist.builder()
                .id(checklistId)
                .title("My Checklist")
                .user(user)
                .build();

        task = Task.builder()
                .id(taskId)
                .title("My Task")
                .description("Task description")
                .completed(false)
                .priority("HIGH")
                .dueDate(LocalDateTime.now().plusDays(1))
                .user(user)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Create Tests")
    class CreateTests {
        @Test
        @DisplayName("Should create task successfully without checklist")
        void testCreateWithoutChecklist() {
            TaskRequestDTO request = TaskRequestDTO.builder()
                    .title("My Task")
                    .description("Task description")
                    .priority("HIGH")
                    .build();
            
            given(taskRepository.save(any(Task.class))).willReturn(task);

            TaskResponseDTO response = taskService.create(request, user);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(taskId);
            assertThat(response.title()).isEqualTo("My Task");
            assertThat(response.completed()).isFalse();
            verify(taskRepository).save(any(Task.class));
        }

        @Test
        @DisplayName("Should create task successfully with checklist")
        void testCreateWithChecklist() {
            TaskRequestDTO request = TaskRequestDTO.builder()
                    .title("My Task")
                    .checklistId(checklistId)
                    .build();
            
            task.setChecklist(checklist);
            
            given(checklistRepository.findByIdAndUser(checklistId, user)).willReturn(Optional.of(checklist));
            given(taskRepository.save(any(Task.class))).willReturn(task);

            TaskResponseDTO response = taskService.create(request, user);

            assertThat(response).isNotNull();
            assertThat(response.checklistId()).isEqualTo(checklistId);
            verify(checklistRepository).findByIdAndUser(checklistId, user);
            verify(taskRepository).save(any(Task.class));
        }

        @Test
        @DisplayName("Should throw exception when creating task with non-existent checklist")
        void testCreateWithNonExistentChecklist() {
            TaskRequestDTO request = TaskRequestDTO.builder()
                    .title("My Task")
                    .checklistId(checklistId)
                    .build();
            
            given(checklistRepository.findByIdAndUser(checklistId, user)).willReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.create(request, user))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND)
                    .hasMessageContaining("Checklist not found");
        }
    }

    @Nested
    @DisplayName("Find All Tests")
    class FindAllTests {
        @Test
        @DisplayName("Should return all user tasks with filters")
        void testFindAll() {
            given(taskRepository.findByUserWithFilters(user, false, "HIGH", checklistId)).willReturn(List.of(task));

            List<TaskResponseDTO> responses = taskService.findAll(user, false, "HIGH", checklistId);

            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).id()).isEqualTo(taskId);
            verify(taskRepository).findByUserWithFilters(user, false, "HIGH", checklistId);
        }
    }

    @Nested
    @DisplayName("Find Orphans Tests")
    class FindOrphansTests {
        @Test
        @DisplayName("Should return orphan tasks")
        void testFindOrphans() {
            given(taskRepository.findByUserAndChecklistIsNullOrderByCreatedAtDesc(user)).willReturn(List.of(task));

            List<TaskResponseDTO> responses = taskService.findOrphans(user);

            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).id()).isEqualTo(taskId);
            verify(taskRepository).findByUserAndChecklistIsNullOrderByCreatedAtDesc(user);
        }
    }

    @Nested
    @DisplayName("Find By Id Tests")
    class FindByIdTests {
        @Test
        @DisplayName("Should return task when found")
        void testFindById() {
            given(taskRepository.findByIdAndUser(taskId, user)).willReturn(Optional.of(task));

            TaskResponseDTO response = taskService.findById(taskId, user);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(taskId);
            verify(taskRepository).findByIdAndUser(taskId, user);
        }

        @Test
        @DisplayName("Should throw exception when task not found")
        void testFindByIdNotFound() {
            given(taskRepository.findByIdAndUser(taskId, user)).willReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.findById(taskId, user))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND)
                    .hasMessageContaining("Task not found");
        }
    }

    @Nested
    @DisplayName("Update Tests")
    class UpdateTests {
        @Test
        @DisplayName("Should update task successfully without checklist")
        void testUpdateWithoutChecklist() {
            TaskRequestDTO request = TaskRequestDTO.builder()
                    .title("Updated Title")
                    .description("Updated Description")
                    .build();
            
            given(taskRepository.findByIdAndUser(taskId, user)).willReturn(Optional.of(task));
            
            Task updatedTask = Task.builder()
                    .id(taskId)
                    .title("Updated Title")
                    .description("Updated Description")
                    .completed(false)
                    .priority("MEDIUM")
                    .user(user)
                    .build();
            given(taskRepository.save(any(Task.class))).willReturn(updatedTask);

            TaskResponseDTO response = taskService.update(taskId, request, user);

            assertThat(response).isNotNull();
            assertThat(response.title()).isEqualTo("Updated Title");
            verify(taskRepository).findByIdAndUser(taskId, user);
            verify(taskRepository).save(any(Task.class));
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent task")
        void testUpdateNotFound() {
            TaskRequestDTO request = TaskRequestDTO.builder().title("Update").build();
            given(taskRepository.findByIdAndUser(taskId, user)).willReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.update(taskId, request, user))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND)
                    .hasMessageContaining("Task not found");
        }
        
        @Test
        @DisplayName("Should throw exception when updating task with non-existent checklist")
        void testUpdateWithNonExistentChecklist() {
            TaskRequestDTO request = TaskRequestDTO.builder()
                    .title("Update")
                    .checklistId(checklistId)
                    .build();
            
            given(taskRepository.findByIdAndUser(taskId, user)).willReturn(Optional.of(task));
            given(checklistRepository.findByIdAndUser(checklistId, user)).willReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.update(taskId, request, user))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND)
                    .hasMessageContaining("Checklist not found");
        }
    }

    @Nested
    @DisplayName("Complete Tests")
    class CompleteTests {
        @Test
        @DisplayName("Should complete task successfully")
        void testComplete() {
            given(taskRepository.findByIdAndUser(taskId, user)).willReturn(Optional.of(task));
            
            Task completedTask = Task.builder()
                    .id(taskId)
                    .title(task.getTitle())
                    .completed(true)
                    .user(user)
                    .build();
            given(taskRepository.save(any(Task.class))).willReturn(completedTask);

            TaskResponseDTO response = taskService.complete(taskId, user);

            assertThat(response).isNotNull();
            assertThat(response.completed()).isTrue();
            verify(taskRepository).findByIdAndUser(taskId, user);
            verify(taskRepository).save(any(Task.class));
        }
        
        @Test
        @DisplayName("Should throw exception when completing non-existent task")
        void testCompleteNotFound() {
            given(taskRepository.findByIdAndUser(taskId, user)).willReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.complete(taskId, user))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND)
                    .hasMessageContaining("Task not found");
        }
    }

    @Nested
    @DisplayName("Reopen Tests")
    class ReopenTests {
        @Test
        @DisplayName("Should reopen task successfully")
        void testReopen() {
            task.setCompleted(true);
            given(taskRepository.findByIdAndUser(taskId, user)).willReturn(Optional.of(task));
            
            Task reopenedTask = Task.builder()
                    .id(taskId)
                    .title(task.getTitle())
                    .completed(false)
                    .user(user)
                    .build();
            given(taskRepository.save(any(Task.class))).willReturn(reopenedTask);

            TaskResponseDTO response = taskService.reopen(taskId, user);

            assertThat(response).isNotNull();
            assertThat(response.completed()).isFalse();
            verify(taskRepository).findByIdAndUser(taskId, user);
            verify(taskRepository).save(any(Task.class));
        }
    }

    @Nested
    @DisplayName("Delete Tests")
    class DeleteTests {
        @Test
        @DisplayName("Should delete task successfully")
        void testDelete() {
            given(taskRepository.findByIdAndUser(taskId, user)).willReturn(Optional.of(task));

            taskService.delete(taskId, user);

            verify(taskRepository).findByIdAndUser(taskId, user);
            verify(taskRepository).delete(task);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent task")
        void testDeleteNotFound() {
            given(taskRepository.findByIdAndUser(taskId, user)).willReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.delete(taskId, user))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND)
                    .hasMessageContaining("Task not found");
        }
    }
}
