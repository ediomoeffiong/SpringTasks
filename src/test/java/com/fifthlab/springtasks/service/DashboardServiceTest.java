package com.fifthlab.springtasks.service;

import com.fifthlab.springtasks.model.CategoryBreakdownDTO;
import com.fifthlab.springtasks.model.DashboardResponseDTO;
import com.fifthlab.springtasks.model.Task;
import com.fifthlab.springtasks.model.TaskDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DashboardServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private String testUser = "testuser";

    @BeforeEach
    void setUp() {
        // Mock data setup
    }

    @Test
    void testGetDashboardData() {
        // Arrange
        when(taskRepository.countByUsername(testUser)).thenReturn(10L);
        when(taskRepository.countByUsernameAndCompleted(testUser, true)).thenReturn(6L);
        when(taskRepository.countByUsernameAndCompleted(testUser, false)).thenReturn(4L);
        when(taskRepository.countByUsernameAndCompletedFalseAndDueDateBefore(eq(testUser), any(LocalDateTime.class))).thenReturn(1L);

        Task mockTask = Task.builder()
                .id(1L)
                .title("Test Task")
                .completed(false)
                .dueDate(LocalDateTime.now().plusDays(1))
                .createdAt(LocalDateTime.now())
                .build();

        when(taskRepository.findTop10ByUsernameOrderByCreatedAtDesc(testUser))
                .thenReturn(List.of(mockTask));
                
        when(taskRepository.findByUsernameAndCompletedFalseAndDueDateBetweenOrderByDueDateAsc(eq(testUser), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(mockTask));
                
        CategoryBreakdownDTO breakdown = new CategoryBreakdownDTO() {
            @Override
            public String getCategory() { return "Work"; }
            @Override
            public Long getCount() { return 2L; }
        };
        when(taskRepository.getCategoryBreakdownByUsername(testUser))
                .thenReturn(List.of(breakdown));

        // Act
        DashboardResponseDTO result = dashboardService.getDashboardData(testUser);

        // Assert
        assertEquals(10L, result.getTotalTasks());
        assertEquals(6L, result.getCompletedTasks());
        assertEquals(4L, result.getPendingTasks());
        assertEquals(1L, result.getOverdueTasks());
        assertEquals(60.0, result.getCompletionRate());
        assertEquals(1, result.getRecentTasks().size());
        assertEquals(1, result.getCategoryBreakdown().size());
        assertEquals(1, result.getUpcomingDeadlines().size());
        assertEquals("Work", result.getCategoryBreakdown().get(0).getCategory());
        assertEquals(2L, result.getCategoryBreakdown().get(0).getCount());
    }
}
