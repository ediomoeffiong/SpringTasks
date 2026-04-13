package com.fifthlab.springtasks.service;

import com.fifthlab.springtasks.model.*;
import com.fifthlab.springtasks.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import org.springframework.lang.NonNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class TaskService {
    private final TaskRepository taskRepository;
    private final NotificationService notificationService;
    private final CategoryService categoryService;
    private final UserRepository userRepository;
    private final TaskInvitationRepository invitationRepository;

    public String getCurrentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            return auth.getName().toLowerCase().trim();
        }
        return "defaultUser";
    }

    public List<Task> findAll() {
        return taskRepository.findAllForUser(getCurrentUsername());
    }

    public List<Task> findByStatusAndSearch(Boolean completed, String search) {
        String username = getCurrentUsername();
        boolean hasSearch = search != null && !search.trim().isEmpty();

        if (completed == null) {
            return hasSearch ? taskRepository.searchForUser(username, search.trim()) 
                             : taskRepository.findAllForUser(username);
        } else {
            return hasSearch ? taskRepository.searchForUserAndCompleted(username, search.trim(), completed)
                             : taskRepository.findByCompletedForUser(username, completed);
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
        if (task.getUsername() == null || task.getUsername().trim().isEmpty()) {
            task.setUsername(getCurrentUsername());
        } else {
            task.setUsername(task.getUsername().toLowerCase().trim());
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
                // Only allow setting/fixing the owner if it was missing or if the current user owns it
                if (existingTask.getUsername() == null || existingTask.getUsername().trim().isEmpty()) {
                    existingTask.setUsername(task.getUsername());
                }
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

    @Transactional
    public TaskInvitation sendInvitation(Long taskId, String collaboratorUsername) {
        String ownerUsername = getCurrentUsername();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (!task.getUsername().equals(ownerUsername)) {
            throw new SecurityException("Only the owner can send invitations");
        }

        User receiver = userRepository.findByUsername(collaboratorUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + collaboratorUsername));

        if (ownerUsername.equals(collaboratorUsername)) {
            throw new IllegalArgumentException("You cannot invite yourself");
        }

        // Check if already a collaborator
        if (task.getCollaborators().stream().anyMatch(u -> u.getUsername().equals(collaboratorUsername))) {
            throw new IllegalArgumentException("User is already a collaborator");
        }

        // Check if pending invitation exists
        Optional<TaskInvitation> existingInvite = invitationRepository
                .findByTaskAndReceiverAndStatus(task, receiver, InvitationStatus.PENDING);
        if (existingInvite.isPresent()) {
            throw new IllegalArgumentException("A pending invitation already exists for this user");
        }

        User sender = userRepository.findByUsername(ownerUsername)
                .orElseThrow(() -> new IllegalStateException("Owner user not found"));

        TaskInvitation invitation = TaskInvitation.builder()
                .task(task)
                .sender(sender)
                .receiver(receiver)
                .status(InvitationStatus.PENDING)
                .build();

        TaskInvitation saved = invitationRepository.save(invitation);

        notificationService.save(Notification.builder()
                .type("system")
                .title("New Task Invitation")
                .message(ownerUsername + " invited you to collaborate on: \"" + task.getTitle() + "\"")
                .relatedTaskTitle(task.getTitle())
                .unread(true)
                .username(collaboratorUsername)
                .build());

        return saved;
    }

    @Transactional
    public void acceptInvitation(Long invitationId) {
        String currentUsername = getCurrentUsername();
        TaskInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));

        if (!invitation.getReceiver().getUsername().equals(currentUsername)) {
            throw new SecurityException("Only the receiver can accept the invitation");
        }

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalStateException("Invitation is no longer pending");
        }

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.getTask().getCollaborators().add(invitation.getReceiver());
        
        taskRepository.save(invitation.getTask());
        invitationRepository.save(invitation);

        notificationService.save(Notification.builder()
                .type("system")
                .title("Invitation Accepted")
                .message(currentUsername + " accepted your invitation to: \"" + invitation.getTask().getTitle() + "\"")
                .relatedTaskTitle(invitation.getTask().getTitle())
                .unread(true)
                .username(invitation.getSender().getUsername())
                .build());
    }

    @Transactional
    public void declineInvitation(Long invitationId) {
        String currentUsername = getCurrentUsername();
        TaskInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));

        if (!invitation.getReceiver().getUsername().equals(currentUsername)) {
            throw new SecurityException("Only the receiver can decline the invitation");
        }

        invitation.setStatus(InvitationStatus.DECLINED);
        invitationRepository.save(invitation);
    }

    @Transactional
    public void cancelInvitation(Long invitationId) {
        String currentUsername = getCurrentUsername();
        TaskInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));

        if (!invitation.getSender().getUsername().equals(currentUsername)) {
            throw new SecurityException("Only the sender can cancel the invitation");
        }

        if (invitation.getStatus() == InvitationStatus.PENDING) {
            invitationRepository.delete(invitation);
        }
    }

    public List<TaskInvitation> getPendingInvitationsForUser() {
        String username = getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return invitationRepository.findByReceiverAndStatus(user, InvitationStatus.PENDING);
    }

    public List<TaskInvitation> getPendingInvitationsForTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        return invitationRepository.findByTaskAndStatus(task, InvitationStatus.PENDING);
    }

    @Transactional
    public void removeCollaborator(Long taskId, Long collaboratorUserId) {
        String currentUsername = getCurrentUsername();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (!task.getUsername().equals(currentUsername)) {
            throw new SecurityException("Only the owner can remove collaborators");
        }

        task.getCollaborators().removeIf(u -> u.getId().equals(collaboratorUserId));
        taskRepository.save(task);
    }

    public boolean isOwner(Long taskId) {
        String currentId = getCurrentUsername();
        return taskRepository.findById(taskId)
                .map(t -> {
                    if (t.getUsername() == null) return false;
                    String taskOwner = t.getUsername().toLowerCase().trim();
                    if (taskOwner.equals(currentId)) return true;
                    
                    // Robust check: see if the task owner string matches the current user's email
                    // or if the current principal is an email and the task owner is the username.
                    return userRepository.findByUsername(currentId)
                            .map(u -> (u.getEmail() != null && u.getEmail().toLowerCase().trim().equals(taskOwner)))
                            .orElseGet(() -> 
                                userRepository.findByEmail(currentId)
                                    .map(u -> u.getUsername() != null && u.getUsername().toLowerCase().trim().equals(taskOwner))
                                    .orElse(false)
                            );
                })
                .orElse(false);
    }
}
