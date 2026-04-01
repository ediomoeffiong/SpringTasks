package com.fifthlab.springtasks.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponseDTO {
    private long totalTasks;
    private long completedTasks;
    private long pendingTasks;
    private long overdueTasks;
    private double completionRate;
    private List<TaskDTO> recentTasks;
    private List<CategoryBreakdownDTO> categoryBreakdown;
    private List<TaskDTO> upcomingDeadlines;
    private List<ActivityDTO> activityFeed;
}

