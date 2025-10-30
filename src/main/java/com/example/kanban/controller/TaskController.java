package com.example.kanban.controller;

import com.example.kanban.dto.MoveTaskRequest;
import com.example.kanban.dto.TaskRequest;
import com.example.kanban.model.ArchivedTask;
import com.example.kanban.model.Task;
import com.example.kanban.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<Task> listActive() {
        return taskService.getAllActiveTasks();
    }

    @GetMapping("/archived")
    public List<ArchivedTask> listArchived() {
        return taskService.getAllArchivedTasks();
    }

    @PostMapping
    public ResponseEntity<Task> create(@Valid @RequestBody TaskRequest request) {
        Task task = new Task();
        applyRequest(request, task);
        Task created = taskService.createTask(task);
        return ResponseEntity.created(URI.create("/api/tasks/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public Task update(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        Task task = new Task();
        applyRequest(request, task);
        return taskService.updateTask(id, task);
    }

    @PostMapping("/{id}/move")
    public Task move(@PathVariable Long id, @Valid @RequestBody MoveTaskRequest request) {
        return taskService.moveTask(id, request.getStatus());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    private void applyRequest(TaskRequest request, Task task) {
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setAssigneeName(request.getAssigneeName());
        task.setAssigneeEmail(request.getAssigneeEmail());
        task.setDueDate(request.getDueDate());
        task.setStatus(request.getStatus());
    }
}
