package com.fifthlab.springtasks.dto;

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
    private String totalTasksTrend;
    private String completedComment;
    private String pendingComment;
    private String overdueComment;
    private List<TaskDTO> recentTasks;
    private List<CategoryBreakdownDTO> categoryBreakdown;
    private List<TaskDTO> upcomingDeadlines;
    private List<ActivityDTO> activityFeed;
}

