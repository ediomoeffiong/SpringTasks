package com.fifthlab.springtasks.service;

import com.fifthlab.springtasks.model.CategoryBreakdownDTO;
import com.fifthlab.springtasks.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    long countByUsername(String username);
    
    long countByUsernameAndCompleted(String username, boolean completed);
    
    long countByUsernameAndCompletedFalseAndDueDateBefore(String username, LocalDateTime date);
    
    List<Task> findTop10ByUsernameOrderByCreatedAtDesc(String username);
    
    List<Task> findByUsernameAndCompletedFalseAndDueDateBetweenOrderByDueDateAsc(String username, LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT t.category as category, COUNT(t) as count FROM Task t WHERE t.username = :username AND t.category IS NOT NULL GROUP BY t.category")
    List<CategoryBreakdownDTO> getCategoryBreakdownByUsername(String username);
}
