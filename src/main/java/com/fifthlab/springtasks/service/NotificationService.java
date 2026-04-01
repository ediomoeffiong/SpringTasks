package com.fifthlab.springtasks.service;

import com.fifthlab.springtasks.model.Notification;
import com.fifthlab.springtasks.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    private String getCurrentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            return auth.getName();
        }
        return "defaultUser";
    }

    public List<Notification> findAll() {
        return notificationRepository.findByUsernameOrderByCreatedAtDesc(getCurrentUsername());
    }

    public Notification save(Notification notification) {
        if (notification.getUsername() == null) {
            notification.setUsername(getCurrentUsername());
        }
        return notificationRepository.save(notification);
    }

    public void setReadStatus(Long id, boolean unread) {
        if (id == null) return;
        notificationRepository.findById(id).ifPresent(n -> {
            // verify ownership
            if (getCurrentUsername().equals(n.getUsername())) {
                n.setUnread(unread);
                notificationRepository.save(n);
            }
        });
    }

    public void deleteById(Long id) {
        if (id == null) return;
        notificationRepository.findById(id).ifPresent(n -> {
            if (getCurrentUsername().equals(n.getUsername())) {
                notificationRepository.delete(n);
            }
        });
    }

    @Transactional
    public void markAllAsRead() {
        notificationRepository.markAllAsReadByUsername(getCurrentUsername());
    }

    @Transactional
    public void clearAll() {
        notificationRepository.deleteAllByUsername(getCurrentUsername());
    }

    @Transactional
    public void bulkDelete(List<Long> ids) {
        for (Long id : ids) {
            deleteById(id);
        }
    }

    @Transactional
    public void bulkMarkRead(List<Long> ids) {
        for (Long id : ids) {
            setReadStatus(id, false);
        }
    }
}
