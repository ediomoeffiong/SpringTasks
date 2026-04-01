package com.fifthlab.springtasks.controller;

import com.fifthlab.springtasks.model.Task;
import com.fifthlab.springtasks.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.lang.NonNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskRestController {

    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        // Find by status null delegates to fetching all user tasks in current setup
        return ResponseEntity.ok(taskService.findByStatus(null));
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Map<String, Object> payload) {
        Task task = new Task();
        populateTaskFromPayload(task, payload);
        return ResponseEntity.ok(taskService.save(task));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable @NonNull Long id, @RequestBody Map<String, Object> payload) {
        Task task = taskService.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid task ID"));
        populateTaskFromPayload(task, payload);
        return ResponseEntity.ok(taskService.save(task));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable @NonNull Long id) {
        taskService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Void> toggleStatus(@PathVariable @NonNull Long id) {
        taskService.toggleStatus(id);
        return ResponseEntity.ok().build();
    }

    private void populateTaskFromPayload(Task task, Map<String, Object> payload) {
        if (payload.containsKey("title")) {
            task.setTitle((String) payload.get("title"));
        }
        if (payload.containsKey("description")) {
            task.setDescription((String) payload.get("description"));
        }
        // Instead of strict mapping, using generic map helps handle "YYYY-MM-DD"
        if (payload.containsKey("dueDate")) {
            String dueStr = (String) payload.get("dueDate");
            if (dueStr != null && !dueStr.trim().isEmpty()) {
                if (dueStr.length() == 10) {
                    task.setDueDate(LocalDate.parse(dueStr).atStartOfDay());
                } else {
                    task.setDueDate(LocalDateTime.parse(dueStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }
            } else {
                task.setDueDate(null);
            }
        }
        
        if (payload.containsKey("priority")) {
            task.setPriority((String) payload.get("priority"));
        }
        
        if (payload.containsKey("categoryId")) {
            Object catObj = payload.get("categoryId");
            if (catObj instanceof Number) {
                task.setCategoryId(((Number) catObj).longValue());
            } else if (catObj instanceof String && !((String) catObj).isEmpty()) {
                task.setCategoryId(Long.parseLong((String) catObj));
            } else {
                task.setCategoryId(null);
            }
        }
        
        if (payload.containsKey("status")) {
            String status = (String) payload.get("status");
            task.setCompleted("completed".equalsIgnoreCase(status));
        }
    }
}
