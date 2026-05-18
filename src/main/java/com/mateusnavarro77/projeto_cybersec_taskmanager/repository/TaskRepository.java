package com.mateusnavarro77.projeto_cybersec_taskmanager.repository;

import com.mateusnavarro77.projeto_cybersec_taskmanager.entity.Checklist;
import com.mateusnavarro77.projeto_cybersec_taskmanager.entity.Task;
import com.mateusnavarro77.projeto_cybersec_taskmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByUserOrderByCreatedAtDesc(User user);
    List<Task> findByChecklistAndUserOrderByCreatedAtDesc(Checklist checklist, User user);
    Optional<Task> findByIdAndUser(UUID id, User user);
    List<Task> findByUserAndChecklistIsNullOrderByCreatedAtDesc(User user);

    @Query("SELECT t FROM Task t WHERE t.user = :user " +
           "AND (:completed IS NULL OR t.completed = :completed) " +
           "AND (:priority IS NULL OR t.priority = :priority) " +
           "AND (cast(:checklistId as java.util.UUID) IS NULL OR t.checklist.id = :checklistId) " +
           "ORDER BY t.createdAt DESC")
    List<Task> findByUserWithFilters(
            @Param("user") User user,
            @Param("completed") Boolean completed,
            @Param("priority") String priority,
            @Param("checklistId") UUID checklistId
    );
}
