package com.mateusnavarro77.projeto_cybersec_taskmanager.repository;

import com.mateusnavarro77.projeto_cybersec_taskmanager.entity.Checklist;
import com.mateusnavarro77.projeto_cybersec_taskmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChecklistRepository extends JpaRepository<Checklist, UUID> {
    List<Checklist> findByUserOrderByCreatedAtDesc(User user);
    Optional<Checklist> findByIdAndUser(UUID id, User user);
    boolean existsByIdAndUser(UUID id, User user);
}
