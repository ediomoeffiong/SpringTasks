package com.fifthlab.springtasks.controller;

import com.fifthlab.springtasks.model.TaskInvitation;
import com.fifthlab.springtasks.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationRestController {

    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<List<TaskInvitation>> getMyInvitations() {
        return ResponseEntity.ok(taskService.getPendingInvitationsForUser());
    }

    @PostMapping("/tasks/{taskId}/invite")
    public ResponseEntity<TaskInvitation> invite(@PathVariable @NonNull Long taskId, @RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(taskService.sendInvitation(taskId, username));
    }

    @PatchMapping("/{id}/accept")
    public ResponseEntity<Void> accept(@PathVariable @NonNull Long id) {
        taskService.acceptInvitation(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/decline")
    public ResponseEntity<Void> decline(@PathVariable @NonNull Long id) {
        taskService.declineInvitation(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable @NonNull Long id) {
        taskService.cancelInvitation(id);
        return ResponseEntity.ok().build();
    }
}
