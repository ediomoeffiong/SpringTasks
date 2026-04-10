package com.fifthlab.springtasks.repository;

import com.fifthlab.springtasks.model.CategoryBreakdownDTO;
import com.fifthlab.springtasks.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    long countByUsername(String username);
    
    long countByUsernameAndCompleted(String username, boolean completed);
    
    long countByUsernameAndCompletedFalseAndDueDateBefore(String username, LocalDateTime date);
    
    List<Task> findTop10ByUsernameOrderByCreatedAtDesc(String username);
    
    List<Task> findByUsernameAndCompletedFalseAndDueDateBetweenOrderByDueDateAsc(String username, LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT c.name as category, COUNT(t) as count FROM Task t, Category c WHERE t.categoryId = c.id AND t.username = :username GROUP BY c.name")
    List<CategoryBreakdownDTO> getCategoryBreakdownByUsername(@Param("username") String username);

    // Filter methods
    List<Task> findByUsernameOrderByCreatedAtDesc(String username);
    
    List<Task> findByUsernameAndCompletedOrderByCreatedAtDesc(String username, boolean completed);

    // Search methods
    @Query("SELECT t FROM Task t WHERE t.username = :username AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) ORDER BY t.createdAt DESC")
    List<Task> searchByUsernameAndTerm(@Param("username") String username, @Param("searchTerm") String searchTerm);

    @Query("SELECT t FROM Task t WHERE t.username = :username AND t.completed = :completed AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) ORDER BY t.createdAt DESC")
    List<Task> searchByUsernameAndTermAndCompleted(@Param("username") String username, @Param("searchTerm") String searchTerm, @Param("completed") boolean completed);
}
