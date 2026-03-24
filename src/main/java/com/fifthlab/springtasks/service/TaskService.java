package com.fifthlab.springtasks.service;

import com.fifthlab.springtasks.model.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;

    private String getCurrentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            return auth.getName();
        }
        return "defaultUser";
    }

    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    public List<Task> findByStatus(Boolean completed) {
        String username = getCurrentUsername();
        if (completed == null) return taskRepository.findAll();
        // Since we didn't add findByUsernameAndCompleted earlier, we can just filter or use findAll
        // For keeping it simple while converting:
        return taskRepository.findAll().stream()
                .filter(t -> t.isCompleted() == completed && getCurrentUsername().equals(t.getUsername()))
                .toList(); // Note: ideally we should add findByUsernameAndCompleted to Repo, but this is a refactoring of the existing
    }

    public Optional<Task> findById(Long id) {
        return taskRepository.findById(id);
    }

    public Task save(Task task) {
        if (task.getUsername() == null) {
            task.setUsername(getCurrentUsername());
        }
        if (task.getId() != null) {
            // Update flow
            findById(task.getId()).ifPresent(existingTask -> {
                existingTask.setTitle(task.getTitle());
                existingTask.setDescription(task.getDescription());
                existingTask.setCompleted(task.isCompleted());
                existingTask.setDueDate(task.getDueDate());
                existingTask.setCategory(task.getCategory());
                taskRepository.save(existingTask);
            });
            return task;
        } else {
            return taskRepository.save(task);
        }
    }

    public void deleteById(Long id) {
        taskRepository.deleteById(id);
    }

    public void toggleStatus(Long id) {
        findById(id).ifPresent(t -> {
            t.setCompleted(!t.isCompleted());
            taskRepository.save(t);
        });
    }
}
