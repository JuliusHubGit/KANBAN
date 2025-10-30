package com.example.kanban.service;

import com.example.kanban.model.ArchivedTask;
import com.example.kanban.model.Task;
import com.example.kanban.model.TaskStatus;
import com.example.kanban.repository.ArchivedTaskRepository;
import com.example.kanban.repository.TaskRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ArchivedTaskRepository archivedTaskRepository;
    private final NotificationService notificationService;

    public TaskService(TaskRepository taskRepository,
                       ArchivedTaskRepository archivedTaskRepository,
                       NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.archivedTaskRepository = archivedTaskRepository;
        this.notificationService = notificationService;
    }

    public List<Task> getAllActiveTasks() {
        return taskRepository.findAllByOrderByCreatedAtAsc();
    }

    public List<ArchivedTask> getAllArchivedTasks() {
        return archivedTaskRepository.findAll();
    }

    public Task getTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    @Transactional
    public Task createTask(Task task) {
        task.setStatus(task.getStatus() == null ? TaskStatus.TODO : task.getStatus());
        Task saved = taskRepository.save(task);
        if (saved.getStatus() == TaskStatus.DONE) {
            archiveTask(saved);
            notificationService.notifyAssignee(saved, "completed");
            return saved;
        }
        notificationService.notifyAssignee(saved, "created");
        return saved;
    }

    @Transactional
    public Task updateTask(Long id, Task updatedTask) {
        Task existing = getTask(id);
        TaskStatus previousStatus = existing.getStatus();

        existing.setTitle(updatedTask.getTitle());
        existing.setDescription(updatedTask.getDescription());
        existing.setAssigneeName(updatedTask.getAssigneeName());
        existing.setAssigneeEmail(updatedTask.getAssigneeEmail());
        existing.setDueDate(updatedTask.getDueDate());
        existing.setStatus(updatedTask.getStatus());

        if (previousStatus == TaskStatus.DONE && existing.getStatus() != TaskStatus.DONE) {
            archivedTaskRepository.deleteByOriginalTaskId(existing.getId());
        }

        if (existing.getStatus() == TaskStatus.DONE) {
            archiveTask(existing);
            notificationService.notifyAssignee(existing, "completed");
            return existing;
        }

        Task saved = taskRepository.save(existing);
        if (previousStatus != saved.getStatus()) {
            notificationService.notifyAssignee(saved, "status-changed");
        } else {
            notificationService.notifyAssignee(saved, "updated");
        }
        return saved;
    }

    @Transactional
    public Task moveTask(Long id, TaskStatus newStatus) {
        Task task = getTask(id);
        TaskStatus previousStatus = task.getStatus();
        task.setStatus(newStatus);
        if (newStatus == TaskStatus.DONE) {
            archiveTask(task);
            notificationService.notifyAssignee(task, "completed");
            return task;
        }
        if (previousStatus == TaskStatus.DONE && newStatus != TaskStatus.DONE) {
            archivedTaskRepository.deleteByOriginalTaskId(task.getId());
        }
        Task saved = taskRepository.save(task);
        notificationService.notifyAssignee(saved, "status-changed");
        return saved;
    }

    @Transactional
    public void deleteTask(Long id) {
        archivedTaskRepository.deleteByOriginalTaskId(id);
        taskRepository.deleteById(id);
    }

    @Transactional
    protected void archiveTask(Task task) {
        if (!archivedTaskRepository.existsByOriginalTaskId(task.getId())) {
            ArchivedTask archivedTask = new ArchivedTask(task);
            archivedTaskRepository.save(archivedTask);
        }
        taskRepository.save(task);
    }
}
