package com.fifthlab.springtasks.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityDTO {
    private String type; // e.g. completed, created, updated
    private String action; // e.g. "Completed task", "Created a new task"
    private String taskTitle;
    private LocalDateTime time;
}
