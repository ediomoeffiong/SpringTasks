package com.fifthlab.springtasks.dto;

import com.fifthlab.springtasks.model.Task;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {
    private Long id;
    private String title;
    private String status;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    
    private String username;
    
    public static TaskDTO fromEntity(Task task) {
        return new TaskDTO(
            task.getId(),
            task.getTitle(),
            task.isCompleted() ? "completed" : "pending",
            task.getDueDate(),
            task.getCreatedAt(),
            task.getUsername()
        );
    }
}
