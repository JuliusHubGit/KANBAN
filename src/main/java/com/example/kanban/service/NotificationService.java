package com.example.kanban.service;

import com.example.kanban.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${notification.n8n-webhook-url:}")
    private String webhookUrl;

    public void notifyAssignee(Task task, String action) {
        if (webhookUrl == null || webhookUrl.isBlank() || task.getAssigneeEmail() == null) {
            log.debug("Webhook URL or assignee email not configured; skipping notification.");
            return;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("taskId", task.getId());
        payload.put("title", task.getTitle());
        payload.put("action", action);
        payload.put("assigneeName", task.getAssigneeName());
        payload.put("assigneeEmail", task.getAssigneeEmail());
        payload.put("dueDate", task.getDueDate());
        payload.put("status", task.getStatus());

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.postForEntity(webhookUrl, new HttpEntity<>(payload, headers), Void.class);
            log.info("Sent notification for task {} via webhook", task.getId());
        } catch (Exception ex) {
            log.error("Failed to notify webhook", ex);
        }
    }
}
