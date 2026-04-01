package com.fifthlab.springtasks.repository;

import com.fifthlab.springtasks.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUsernameOrderByCreatedAtDesc(String username);

    @Modifying
    @Query("UPDATE Notification n SET n.unread = false WHERE n.username = :username")
    void markAllAsReadByUsername(String username);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.username = :username")
    void deleteAllByUsername(String username);
}
