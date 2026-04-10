package com.fifthlab.springtasks.service;

import com.fifthlab.springtasks.model.ActivityDTO;
import com.fifthlab.springtasks.model.CategoryBreakdownDTO;
import com.fifthlab.springtasks.model.DashboardResponseDTO;
import com.fifthlab.springtasks.model.Task;
import com.fifthlab.springtasks.model.TaskDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.fifthlab.springtasks.repository.NotificationRepository;
import com.fifthlab.springtasks.repository.TaskRepository;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TaskRepository taskRepository;
    private final NotificationRepository notificationRepository;

    @Cacheable(value = "dashboardData", key = "#username")
    public DashboardResponseDTO getDashboardData(String username) {
        LocalDateTime now = LocalDateTime.now();

        long totalTasks = taskRepository.countByUsername(username);
        long completedTasks = taskRepository.countByUsernameAndCompleted(username, true);
        long pendingTasks = taskRepository.countByUsernameAndCompleted(username, false);
        long overdueTasks = taskRepository.countByUsernameAndCompletedFalseAndDueDateBefore(username, now);

        double completionRate = totalTasks > 0 ? ((double) completedTasks / totalTasks) * 100 : 0.0;
        completionRate = Math.round(completionRate * 100.0) / 100.0; // round to 2 decimals

        List<Task> recentTasksEntities = taskRepository.findTop10ByUsernameOrderByCreatedAtDesc(username);
        List<TaskDTO> recentTasks = recentTasksEntities.stream()
                .map(TaskDTO::fromEntity)
                .collect(Collectors.toList());

        List<CategoryBreakdownDTO> categoryBreakdown = taskRepository.getCategoryBreakdownByUsername(username);

        // Upcoming within 7 days
        LocalDateTime next7Days = now.plusDays(7);
        List<Task> upcomingEntities = taskRepository.findByUsernameAndCompletedFalseAndDueDateBetweenOrderByDueDateAsc(username, now, next7Days);
        List<TaskDTO> upcomingDeadlines = upcomingEntities.stream()
                .map(TaskDTO::fromEntity)
                .collect(Collectors.toList());

        // Activity Feed from Notifications
        List<com.fifthlab.springtasks.model.Notification> recentNotifications = notificationRepository.findByUsernameOrderByCreatedAtDesc(username);
        List<ActivityDTO> activityFeed = recentNotifications.stream()
                .limit(5)
                .map(n -> ActivityDTO.builder()
                        .type(n.getType())
                        .action(n.getType().equalsIgnoreCase("completed") ? "Completed task" 
                                : (n.getType().equalsIgnoreCase("created") ? "Created a new task" : "Updated task"))
                        .taskTitle(n.getRelatedTaskTitle() != null ? n.getRelatedTaskTitle() : "")
                        .time(n.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        // Trends Calculation
        LocalDateTime lastWeek = now.minusDays(7);
        LocalDateTime twoWeeksAgo = now.minusDays(14);
        
        long currentPeriodTotal = taskRepository.countByUsernameAndCreatedAtBetween(username, lastWeek, now);
        long previousPeriodTotal = taskRepository.countByUsernameAndCreatedAtBetween(username, twoWeeksAgo, lastWeek);
        
        String totalTasksTrend = calculateTrend(currentPeriodTotal, previousPeriodTotal);
        
        // Dynamic Comments
        String completedComment = getCompletedComment(completedTasks, totalTasks);
        String pendingComment = getPendingComment(pendingTasks);
        String overdueComment = getOverdueComment(overdueTasks);

        return new DashboardResponseDTO(
                totalTasks,
                completedTasks,
                pendingTasks,
                overdueTasks,
                completionRate,
                totalTasksTrend,
                completedComment,
                pendingComment,
                overdueComment,
                recentTasks,
                categoryBreakdown,
                upcomingDeadlines,
                activityFeed
        );
    }

    private String calculateTrend(long current, long previous) {
        if (previous == 0) return current > 0 ? "+100% from last week" : "0% from last week";
        long diff = current - previous;
        double pct = (double) diff / previous * 100;
        return String.format("%s%.0f%% from last week", diff >= 0 ? "+" : "", pct);
    }

    private String getCompletedComment(long completed, long total) {
        if (total == 0) return "Start your journey today!";
        double rate = (double) completed / total;
        if (rate >= 0.8) return "You're on fire! 🔥";
        if (rate >= 0.5) return "Great job keeping up!";
        return "Keep pushing forward!";
    }

    private String getPendingComment(long pending) {
        if (pending == 0) return "All caught up! 🎉";
        if (pending > 10) return "Quite a lot on your plate.";
        return "Manageable workload.";
    }

    private String getOverdueComment(long overdue) {
        if (overdue == 0) return "No overdue tasks. Perfect!";
        return overdue + " tasks need immediate attention!";
    }
    
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            return authentication.getName();
        }
        return "defaultUser"; // Fallback if no auth or anonymous
    }
}
