package com.fifthlab.springtasks.controller;

import com.fifthlab.springtasks.model.Milestone;
import com.fifthlab.springtasks.service.MilestoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.lang.NonNull;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MilestoneRestController {

    private final MilestoneService milestoneService;

    @GetMapping("/tasks/{taskId}/milestones")
    public ResponseEntity<List<Milestone>> getMilestones(@PathVariable @NonNull Long taskId) {
        return ResponseEntity.ok(milestoneService.findByTaskId(taskId));
    }

    @PostMapping("/tasks/{taskId}/milestones")
    public ResponseEntity<Milestone> addMilestone(@PathVariable @NonNull Long taskId, @RequestBody Milestone milestone) {
        return ResponseEntity.ok(milestoneService.addMilestone(taskId, milestone));
    }

    @PutMapping("/milestones/{id}")
    public ResponseEntity<Milestone> updateMilestone(@PathVariable @NonNull Long id, @RequestBody Milestone milestone) {
        return ResponseEntity.ok(milestoneService.updateMilestone(id, milestone));
    }

    @DeleteMapping("/milestones/{id}")
    public ResponseEntity<Void> deleteMilestone(@PathVariable @NonNull Long id) {
        milestoneService.deleteMilestone(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/milestones/{id}/toggle")
    public ResponseEntity<Void> toggleMilestone(@PathVariable @NonNull Long id) {
        milestoneService.toggleMilestone(id);
        return ResponseEntity.ok().build();
    }
}
