package com.fifthlab.springtasks.service;

import com.fifthlab.springtasks.model.Milestone;
import com.fifthlab.springtasks.model.Task;
import com.fifthlab.springtasks.repository.MilestoneRepository;
import com.fifthlab.springtasks.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MilestoneService {
    private final MilestoneRepository milestoneRepository;
    private final TaskRepository taskRepository;

    public List<Milestone> findByTaskId(Long taskId) {
        return milestoneRepository.findByTaskId(taskId);
    }

    public Optional<Milestone> findById(Long id) {
        return milestoneRepository.findById(id);
    }

    @Transactional
    public Milestone addMilestone(Long taskId, Milestone milestone) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        milestone.setTask(task);
        Milestone saved = milestoneRepository.save(milestone);
        updateTaskProgress(task);
        return saved;
    }

    @Transactional
    public Milestone updateMilestone(Long id, Milestone updated) {
        Milestone existing = milestoneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Milestone not found"));
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setCompleted(updated.isCompleted());
        existing.setDueDate(updated.getDueDate());
        
        Milestone saved = milestoneRepository.save(existing);
        updateTaskProgress(existing.getTask());
        return saved;
    }

    @Transactional
    public void deleteMilestone(Long id) {
        Milestone milestone = milestoneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Milestone not found"));
        Task task = milestone.getTask();
        milestoneRepository.delete(milestone);
        updateTaskProgress(task);
    }

    @Transactional
    public void toggleMilestone(Long id) {
        Milestone milestone = milestoneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Milestone not found"));
        milestone.setCompleted(!milestone.isCompleted());
        milestoneRepository.save(milestone);
        updateTaskProgress(milestone.getTask());
    }

    private void updateTaskProgress(Task task) {
        List<Milestone> milestones = milestoneRepository.findByTaskId(task.getId());
        if (!milestones.isEmpty()) {
            boolean allCompleted = milestones.stream().allMatch(Milestone::isCompleted);
            task.setCompleted(allCompleted);
            taskRepository.save(task);
        }
    }
}
