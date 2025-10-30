package com.example.kanban.dto;

import com.example.kanban.model.TaskStatus;
import jakarta.validation.constraints.NotNull;

public class MoveTaskRequest {
    @NotNull
    private TaskStatus status;

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}
