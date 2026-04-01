package com.fifthlab.springtasks.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String roles; // Comma separated roles like "ROLE_USER,ROLE_ADMIN"

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // --- Profile & Settings Expansions ---
    @Column(name = "full_name")
    private String fullName;

    private String location;

    @Column(length = 500)
    private String bio;

    @Column(name = "two_factor_enabled")
    private boolean twoFactorEnabled = false;

    @Column(name = "email_notifications_enabled")
    private boolean emailNotificationsEnabled = true;

    @Column(name = "task_reminders_enabled")
    private boolean taskRemindersEnabled = true;

    @Column(name = "product_updates_enabled")
    private boolean productUpdatesEnabled = false;

    @Column(name = "theme_preference")
    private String themePreference = "system";

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
