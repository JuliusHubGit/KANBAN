package com.example.kanban;

import com.example.kanban.model.Task;
import com.example.kanban.model.TaskStatus;
import com.example.kanban.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @Test
    void createTaskReturns201AndPayload() throws Exception {
        Task response = new Task();
        response.setId(1L);
        response.setTitle("New Task");
        response.setStatus(TaskStatus.TODO);
        response.setDueDate(LocalDate.of(2024, 12, 31));

        Mockito.when(taskService.createTask(any(Task.class))).thenReturn(response);

        String body = "{" +
                "\"title\":\"New Task\"," +
                "\"description\":\"Description\"," +
                "\"status\":\"TODO\"" +
                "}";

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/tasks/1"))
                .andExpect(jsonPath("$.title").value("New Task"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void moveTaskReturnsUpdatedTask() throws Exception {
        Task response = new Task();
        response.setId(5L);
        response.setTitle("Existing Task");
        response.setStatus(TaskStatus.DONE);

        Mockito.when(taskService.moveTask(eq(5L), eq(TaskStatus.DONE))).thenReturn(response);

        String body = objectMapper.writeValueAsString(new MoveRequest(TaskStatus.DONE));

        mockMvc.perform(post("/api/tasks/5/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    private record MoveRequest(TaskStatus status) {}
}
