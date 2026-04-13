package com.fifthlab.springtasks.repository;

import com.fifthlab.springtasks.dto.CategoryBreakdownDTO;
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
    
    long countByUsernameAndCreatedAtBetween(String username, LocalDateTime start, LocalDateTime end);
    
    long countByUsernameAndCompletedAndUpdatedAtBetween(String username, boolean completed, LocalDateTime start, LocalDateTime end);
    
    List<Task> findTop10ByUsernameOrderByCreatedAtDesc(String username);
    
    List<Task> findByUsernameAndCompletedFalseAndDueDateBetweenOrderByDueDateAsc(String username, LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT c.name as category, COUNT(t) as count FROM Task t, Category c WHERE t.categoryId = c.id AND t.username = :username GROUP BY c.name")
    List<CategoryBreakdownDTO> getCategoryBreakdownByUsername(@Param("username") String username);

    List<Task> findByUsernameOrderByCreatedAtDesc(String username);

    // Filter methods (Collaboration Aware)
    @Query("SELECT t FROM Task t LEFT JOIN t.collaborators c WHERE t.username = :username OR c.username = :username ORDER BY t.createdAt DESC")
    List<Task> findAllForUser(@Param("username") String username);
    
    @Query("SELECT t FROM Task t LEFT JOIN t.collaborators c WHERE (t.username = :username OR c.username = :username) AND t.completed = :completed ORDER BY t.createdAt DESC")
    List<Task> findByCompletedForUser(@Param("username") String username, @Param("completed") boolean completed);

    // Search methods (Collaboration Aware)
    @Query("SELECT t FROM Task t LEFT JOIN t.collaborators c WHERE (t.username = :username OR c.username = :username) AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) ORDER BY t.createdAt DESC")
    List<Task> searchForUser(@Param("username") String username, @Param("searchTerm") String searchTerm);

    @Query("SELECT t FROM Task t LEFT JOIN t.collaborators c WHERE (t.username = :username OR c.username = :username) AND t.completed = :completed AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) ORDER BY t.createdAt DESC")
    List<Task> searchForUserAndCompleted(@Param("username") String username, @Param("searchTerm") String searchTerm, @Param("completed") boolean completed);
}
