package com.fifthlab.springtasks.controller;

import com.fifthlab.springtasks.model.DashboardResponseDTO;
import com.fifthlab.springtasks.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardRestController {

    private final DashboardService dashboardService;

    // Original monolith response for Thymeleaf
    @GetMapping("/all")
    public ResponseEntity<DashboardResponseDTO> getDashboardData() {
        String username = dashboardService.getCurrentUsername();
        return ResponseEntity.ok(dashboardService.getDashboardData(username));
    }

    // --- Modern React Query Endpoints ---

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        DashboardResponseDTO data = dashboardService.getDashboardData(dashboardService.getCurrentUsername());
        return ResponseEntity.ok(Map.of(
                "totalTasks", data.getTotalTasks(),
                "completedTasks", data.getCompletedTasks(),
                "pendingTasks", data.getPendingTasks(),
                "overdueTasks", data.getOverdueTasks(),
                "completionRate", data.getCompletionRate()
        ));
    }

    @GetMapping("/recent-tasks")
    public ResponseEntity<?> getRecentTasks() {
        DashboardResponseDTO data = dashboardService.getDashboardData(dashboardService.getCurrentUsername());
        return ResponseEntity.ok(data.getRecentTasks());
    }

    @GetMapping("/upcoming-events")
    public ResponseEntity<?> getUpcomingEvents() {
        DashboardResponseDTO data = dashboardService.getDashboardData(dashboardService.getCurrentUsername());
        return ResponseEntity.ok(data.getUpcomingDeadlines());
    }

    @GetMapping("/category-breakdown")
    public ResponseEntity<?> getCategoryBreakdown() {
        DashboardResponseDTO data = dashboardService.getDashboardData(dashboardService.getCurrentUsername());
        return ResponseEntity.ok(data.getCategoryBreakdown());
    }
    
    @GetMapping("/productivity")
    public ResponseEntity<?> getProductivity() {
        // Placeholder for advanced chart datasets
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Placeholder for productivity timeline datasets"
        ));
    }
}
