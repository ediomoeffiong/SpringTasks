package com.fifthlab.springtasks.service;

import com.fifthlab.springtasks.model.Notification;
import com.fifthlab.springtasks.model.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import org.springframework.lang.NonNull;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class TaskService {
    private final TaskRepository taskRepository;
    private final NotificationService notificationService;

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
                .filter(t -> t.isCompleted() == completed && username.equals(t.getUsername()))
                .toList(); // Note: ideally we should add findByUsernameAndCompleted to Repo, but this is a refactoring of the existing
    }

    public Optional<Task> findById(@NonNull Long id) {
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
                existingTask.setCategoryId(task.getCategoryId());
                existingTask.setPriority(task.getPriority());
                taskRepository.save(existingTask);
            });
            return task;
        } else {
            Task saved = taskRepository.save(task);
            notificationService.save(Notification.builder()
                .type("created")
                .title("Task Created")
                .message("You added a new task: \"" + saved.getTitle() + "\"")
                .relatedTaskTitle(saved.getTitle())
                .unread(true)
                .username(saved.getUsername())
                .build());
            return saved;
        }
    }

    public void deleteById(@NonNull Long id) {
        taskRepository.deleteById(id);
    }

    public void toggleStatus(@NonNull Long id) {
        findById(id).ifPresent(t -> {
            boolean isNowCompleted = !t.isCompleted();
            t.setCompleted(isNowCompleted);
            taskRepository.save(t);
            
            if (isNowCompleted) {
                notificationService.save(Notification.builder()
                    .type("completed")
                    .title("Task Completed!")
                    .message("You successfully completed the task: \"" + t.getTitle() + "\"")
                    .relatedTaskTitle(t.getTitle())
                    .unread(true)
                    .username(t.getUsername())
                    .build());
            }
        });
    }
}
