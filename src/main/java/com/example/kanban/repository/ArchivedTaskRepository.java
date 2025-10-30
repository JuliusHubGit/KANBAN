package com.example.kanban.repository;

import com.example.kanban.model.ArchivedTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArchivedTaskRepository extends JpaRepository<ArchivedTask, Long> {
    boolean existsByOriginalTaskId(Long originalTaskId);
    void deleteByOriginalTaskId(Long originalTaskId);
}
