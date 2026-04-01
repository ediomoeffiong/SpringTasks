package com.fifthlab.springtasks.controller;

import com.fifthlab.springtasks.model.Notification;
import com.fifthlab.springtasks.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationRestController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<Notification>> getAll() {
        return ResponseEntity.ok(notificationService.findAll());
    }

    @PostMapping
    public ResponseEntity<Notification> create(@RequestBody Notification notification) {
        return ResponseEntity.ok(notificationService.save(notification));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> toggleRead(@PathVariable Long id, @RequestBody Map<String, Boolean> payload) {
        Boolean unread = payload.get("unread");
        if (unread != null) {
            notificationService.setReadStatus(id, unread);
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        notificationService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<Void> markAllRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/clear-all")
    public ResponseEntity<Void> clearAll() {
        notificationService.clearAll();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/bulk-read")
    public ResponseEntity<Void> bulkRead(@RequestBody List<Long> ids) {
        notificationService.bulkMarkRead(ids);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/bulk-delete")
    public ResponseEntity<Void> bulkDelete(@RequestBody List<Long> ids) {
        notificationService.bulkDelete(ids);
        return ResponseEntity.ok().build();
    }
}
