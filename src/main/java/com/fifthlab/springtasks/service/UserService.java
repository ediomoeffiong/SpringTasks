package com.fifthlab.springtasks.service;

import com.fifthlab.springtasks.model.User;
import com.fifthlab.springtasks.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public String getCurrentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            return auth.getName();
        }
        return null;
    }

    public Optional<User> getCurrentUser() {
        String username = getCurrentUsername();
        if (username == null) return Optional.empty();
        return userRepository.findByUsername(username);
    }

    public User updateProfile(User updateData) {
        User user = getCurrentUser().orElseThrow(() -> new RuntimeException("User not found"));
        user.setFullName(updateData.getFullName());
        user.setLocation(updateData.getLocation());
        user.setBio(updateData.getBio());
        return userRepository.save(user);
    }

    public User updateSettings(User updateData) {
        User user = getCurrentUser().orElseThrow(() -> new RuntimeException("User not found"));
        user.setTwoFactorEnabled(updateData.isTwoFactorEnabled());
        user.setEmailNotificationsEnabled(updateData.isEmailNotificationsEnabled());
        user.setTaskRemindersEnabled(updateData.isTaskRemindersEnabled());
        user.setProductUpdatesEnabled(updateData.isProductUpdatesEnabled());
        return userRepository.save(user);
    }

    public User updateTheme(String themePreference) {
        User user = getCurrentUser().orElseThrow(() -> new RuntimeException("User not found"));
        user.setThemePreference(themePreference);
        return userRepository.save(user);
    }

    public boolean updatePassword(String currentPassword, String newPassword) {
        User user = getCurrentUser().orElseThrow(() -> new RuntimeException("User not found"));
        if (passwordEncoder.matches(currentPassword, user.getPassword())) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public void deleteAccount() {
        User user = getCurrentUser().orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
    }
}
