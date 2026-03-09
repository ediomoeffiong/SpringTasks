package com.fifthlab.springtasks.service;

import com.fifthlab.springtasks.model.Task;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class TaskService {
    private final List<Task> tasks = new ArrayList<>();
    private final AtomicLong counter = new AtomicLong();

    public TaskService() {
        // Mock some initial data
        save(Task.builder().title("Review PRD").description("Analyze requirements for the SpringTasks frontend.").completed(true).build());
        save(Task.builder().title("Setup Project").description("Initialize Spring Boot project and dependencies.").completed(true).build());
        save(Task.builder().title("Implement Thymeleaf Templates").description("Create UI for task management.").completed(false).build());
    }

    public List<Task> findAll() {
        return new ArrayList<>(tasks);
    }

    public List<Task> findByStatus(Boolean completed) {
        if (completed == null) return findAll();
        return tasks.stream()
                .filter(t -> t.isCompleted() == completed)
                .collect(Collectors.toList());
    }

    public Optional<Task> findById(Long id) {
        return tasks.stream().filter(t -> t.getId().equals(id)).findFirst();
    }

    public Task save(Task task) {
        if (task.getId() == null) {
            task.setId(counter.incrementAndGet());
            task.setCreatedAt(LocalDateTime.now());
            task.setUpdatedAt(LocalDateTime.now());
            tasks.add(task);
        } else {
            findById(task.getId()).ifPresent(existingTask -> {
                existingTask.setTitle(task.getTitle());
                existingTask.setDescription(task.getDescription());
                existingTask.setCompleted(task.isCompleted());
                existingTask.setUpdatedAt(LocalDateTime.now());
            });
        }
        return task;
    }

    public void deleteById(Long id) {
        tasks.removeIf(t -> t.getId().equals(id));
    }

    public void toggleStatus(Long id) {
        findById(id).ifPresent(t -> {
            t.setCompleted(!t.isCompleted());
            t.setUpdatedAt(LocalDateTime.now());
        });
    }
}
