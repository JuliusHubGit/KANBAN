package com.example.kanban;

import com.example.kanban.model.Task;
import com.example.kanban.model.TaskStatus;
import com.example.kanban.repository.ArchivedTaskRepository;
import com.example.kanban.repository.TaskRepository;
import com.example.kanban.service.NotificationService;
import com.example.kanban.service.TaskService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest
class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ArchivedTaskRepository archivedTaskRepository;

    @MockBean
    private NotificationService notificationService;

    @AfterEach
    void cleanUp() {
        archivedTaskRepository.deleteAll();
        taskRepository.deleteAll();
    }

    @Test
    void getTaskMissingThrows404() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> taskService.getTask(999L));
        assertEquals(404, exception.getStatusCode().value());
    }

    @Test
    void createTaskWithDoneStatusArchivesAndNotifiesCompleted() {
        Task task = new Task();
        task.setTitle("Done Task");
        task.setStatus(TaskStatus.DONE);
        task.setAssigneeEmail("done@example.com");

        Task saved = taskService.createTask(task);

        assertThat(archivedTaskRepository.existsByOriginalTaskId(saved.getId())).isTrue();
        verify(notificationService).notifyAssignee(any(Task.class), eq("completed"));
        verify(notificationService, never()).notifyAssignee(any(Task.class), eq("created"));
    }

    @Test
    void moveTaskToDoneArchivesAndNotifiesCompleted() {
        Task task = new Task();
        task.setTitle("Active Task");
        task.setStatus(TaskStatus.TODO);
        task.setAssigneeEmail("active@example.com");

        Task created = taskService.createTask(task);
        Mockito.reset(notificationService);

        Task updated = taskService.moveTask(created.getId(), TaskStatus.DONE);

        assertThat(updated.getStatus()).isEqualTo(TaskStatus.DONE);
        assertThat(archivedTaskRepository.existsByOriginalTaskId(updated.getId())).isTrue();
        verify(notificationService).notifyAssignee(any(Task.class), eq("completed"));
    }
}
