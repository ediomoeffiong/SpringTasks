package com.fifthlab.springtasks.controller;

import com.fifthlab.springtasks.model.User;
import com.fifthlab.springtasks.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserRestController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        return userService.getCurrentUser()
                .map(user -> {
                    // Hide password for the frontend response
                    user.setPassword(null);
                    return ResponseEntity.ok(user);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(@RequestBody User user) {
        User saved = userService.updateProfile(user);
        saved.setPassword(null);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/settings")
    public ResponseEntity<User> updateSettings(@RequestBody User user) {
        User saved = userService.updateSettings(user);
        saved.setPassword(null);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/theme")
    public ResponseEntity<User> updateTheme(@RequestBody Map<String, String> payload) {
        String theme = payload.get("themePreference");
        User saved = userService.updateTheme(theme);
        saved.setPassword(null);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(@RequestBody Map<String, String> payload) {
        String currentPassword = payload.get("currentPassword");
        String newPassword = payload.get("newPassword");
        boolean success = userService.updatePassword(currentPassword, newPassword);
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().body("Incorrect current password.");
        }
    }

    @DeleteMapping("/account")
    public ResponseEntity<Void> deleteAccount() {
        userService.deleteAccount();
        return ResponseEntity.ok().build();
    }
}
