package com.fifthlab.springtasks.controller;

import com.fifthlab.springtasks.model.Task;
import com.fifthlab.springtasks.model.User;
import com.fifthlab.springtasks.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class CollaborationRestController {

    private final TaskService taskService;

    @GetMapping("/{taskId}/collaborators")
    public ResponseEntity<Set<User>> getCollaborators(@PathVariable @NonNull Long taskId) {
        return taskService.findById(taskId)
                .map(Task::getCollaborators)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{taskId}/pending-invitations")
    public ResponseEntity<List<com.fifthlab.springtasks.model.TaskInvitation>> getPendingInvitations(@PathVariable @NonNull Long taskId) {
        return ResponseEntity.ok(taskService.getPendingInvitationsForTask(taskId));
    }

    @DeleteMapping("/{taskId}/collaborators/{userId}")
    public ResponseEntity<Void> removeCollaborator(@PathVariable @NonNull Long taskId, @PathVariable @NonNull Long userId) {
        taskService.removeCollaborator(taskId, userId);
        return ResponseEntity.ok().build();
    }
}
