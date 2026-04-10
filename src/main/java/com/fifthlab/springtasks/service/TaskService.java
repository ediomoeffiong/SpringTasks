package com.fifthlab.springtasks.service;

import com.fifthlab.springtasks.model.Notification;
import com.fifthlab.springtasks.model.Task;
import com.fifthlab.springtasks.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import org.springframework.lang.NonNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class TaskService {
    private final TaskRepository taskRepository;
    private final NotificationService notificationService;
    private final CategoryService categoryService;

    private String getCurrentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            return auth.getName();
        }
        return "defaultUser";
    }

    public List<Task> findAll() {
        return taskRepository.findByUsernameOrderByCreatedAtDesc(getCurrentUsername());
    }

    public List<Task> findByStatusAndSearch(Boolean completed, String search) {
        String username = getCurrentUsername();
        boolean hasSearch = search != null && !search.trim().isEmpty();

        if (completed == null) {
            return hasSearch ? taskRepository.searchByUsernameAndTerm(username, search.trim()) 
                             : taskRepository.findByUsernameOrderByCreatedAtDesc(username);
        } else {
            return hasSearch ? taskRepository.searchByUsernameAndTermAndCompleted(username, search.trim(), completed)
                             : taskRepository.findByUsernameAndCompletedOrderByCreatedAtDesc(username, completed);
        }
    }

    public Optional<Task> findById(@NonNull Long id) {
        return taskRepository.findById(id);
    }

    @Caching(evict = {
        @CacheEvict(value = "dashboardData", key = "#task.username", condition = "#task.username != null"),
        @CacheEvict(value = "dashboardData", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()", condition = "#task.username == null")
    })
    public Task save(Task task) {
        if (task.getUsername() == null) {
            task.setUsername(getCurrentUsername());
        }
        if (task.getCategoryId() == null) {
            task.setCategoryId(categoryService.ensureUncategorizedExists(task.getUsername()).getId());
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

    @CacheEvict(value = "dashboardData", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    public void deleteById(@NonNull Long id) {
        taskRepository.deleteById(id);
    }

    @CacheEvict(value = "dashboardData", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
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
