package com.mateusnavarro77.projeto_cybersec_taskmanager.service;

import com.mateusnavarro77.projeto_cybersec_taskmanager.dto.ChecklistRequestDTO;
import com.mateusnavarro77.projeto_cybersec_taskmanager.dto.ChecklistResponseDTO;
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
public class ChecklistServiceTest {

    @Mock
    private ChecklistRepository checklistRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private ChecklistService checklistService;

    private User user;
    private Checklist checklist;
    private final UUID checklistId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

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
                .description("Checklist description")
                .user(user)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Create Tests")
    class CreateTests {
        @Test
        @DisplayName("Should create checklist successfully")
        void testCreate() {
            ChecklistRequestDTO request = new ChecklistRequestDTO("My Checklist", "Checklist description");
            given(checklistRepository.save(any(Checklist.class))).willReturn(checklist);

            ChecklistResponseDTO response = checklistService.create(request, user);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(checklistId);
            assertThat(response.title()).isEqualTo("My Checklist");
            assertThat(response.description()).isEqualTo("Checklist description");
            verify(checklistRepository).save(any(Checklist.class));
        }
    }

    @Nested
    @DisplayName("Find All Tests")
    class FindAllTests {
        @Test
        @DisplayName("Should return all user checklists")
        void testFindAll() {
            given(checklistRepository.findByUserOrderByCreatedAtDesc(user)).willReturn(List.of(checklist));

            List<ChecklistResponseDTO> responses = checklistService.findAll(user);

            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).id()).isEqualTo(checklistId);
            verify(checklistRepository).findByUserOrderByCreatedAtDesc(user);
        }
    }

    @Nested
    @DisplayName("Find By Id Tests")
    class FindByIdTests {
        @Test
        @DisplayName("Should return checklist when found")
        void testFindById() {
            given(checklistRepository.findByIdAndUser(checklistId, user)).willReturn(Optional.of(checklist));

            ChecklistResponseDTO response = checklistService.findById(checklistId, user);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(checklistId);
            verify(checklistRepository).findByIdAndUser(checklistId, user);
        }

        @Test
        @DisplayName("Should throw exception when checklist not found")
        void testFindByIdNotFound() {
            given(checklistRepository.findByIdAndUser(checklistId, user)).willReturn(Optional.empty());

            assertThatThrownBy(() -> checklistService.findById(checklistId, user))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND)
                    .hasMessageContaining("Checklist not found");
        }
    }

    @Nested
    @DisplayName("Update Tests")
    class UpdateTests {
        @Test
        @DisplayName("Should update checklist successfully")
        void testUpdate() {
            ChecklistRequestDTO request = new ChecklistRequestDTO("Updated Title", "Updated Description");
            given(checklistRepository.findByIdAndUser(checklistId, user)).willReturn(Optional.of(checklist));
            
            Checklist updatedChecklist = Checklist.builder()
                    .id(checklistId)
                    .title("Updated Title")
                    .description("Updated Description")
                    .user(user)
                    .createdAt(checklist.getCreatedAt())
                    .updatedAt(LocalDateTime.now())
                    .build();
            given(checklistRepository.save(any(Checklist.class))).willReturn(updatedChecklist);

            ChecklistResponseDTO response = checklistService.update(checklistId, request, user);

            assertThat(response).isNotNull();
            assertThat(response.title()).isEqualTo("Updated Title");
            assertThat(response.description()).isEqualTo("Updated Description");
            verify(checklistRepository).findByIdAndUser(checklistId, user);
            verify(checklistRepository).save(any(Checklist.class));
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent checklist")
        void testUpdateNotFound() {
            ChecklistRequestDTO request = new ChecklistRequestDTO("Updated Title", "Updated Description");
            given(checklistRepository.findByIdAndUser(checklistId, user)).willReturn(Optional.empty());

            assertThatThrownBy(() -> checklistService.update(checklistId, request, user))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND)
                    .hasMessageContaining("Checklist not found");
        }
    }

    @Nested
    @DisplayName("Delete Tests")
    class DeleteTests {
        @Test
        @DisplayName("Should delete checklist successfully")
        void testDelete() {
            given(checklistRepository.findByIdAndUser(checklistId, user)).willReturn(Optional.of(checklist));

            checklistService.delete(checklistId, user);

            verify(checklistRepository).findByIdAndUser(checklistId, user);
            verify(checklistRepository).delete(checklist);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent checklist")
        void testDeleteNotFound() {
            given(checklistRepository.findByIdAndUser(checklistId, user)).willReturn(Optional.empty());

            assertThatThrownBy(() -> checklistService.delete(checklistId, user))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND)
                    .hasMessageContaining("Checklist not found");
        }
    }

    @Nested
    @DisplayName("List Tasks Tests")
    class ListTasksTests {
        @Test
        @DisplayName("Should list tasks of checklist successfully")
        void testListTasks() {
            Task task = Task.builder()
                    .id(UUID.randomUUID())
                    .title("Task 1")
                    .user(user)
                    .checklist(checklist)
                    .completed(false)
                    .priority("HIGH")
                    .build();
            
            given(checklistRepository.findByIdAndUser(checklistId, user)).willReturn(Optional.of(checklist));
            given(taskRepository.findByChecklistAndUserOrderByCreatedAtDesc(checklist, user)).willReturn(List.of(task));

            List<?> tasks = checklistService.listTasks(checklistId, user);

            assertThat(tasks).hasSize(1);
            assertThat(tasks.get(0)).isEqualTo(task);
            verify(checklistRepository).findByIdAndUser(checklistId, user);
            verify(taskRepository).findByChecklistAndUserOrderByCreatedAtDesc(checklist, user);
        }

        @Test
        @DisplayName("Should throw exception when listing tasks of non-existent checklist")
        void testListTasksNotFound() {
            given(checklistRepository.findByIdAndUser(checklistId, user)).willReturn(Optional.empty());

            assertThatThrownBy(() -> checklistService.listTasks(checklistId, user))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND)
                    .hasMessageContaining("Checklist not found");
        }
    }
}
