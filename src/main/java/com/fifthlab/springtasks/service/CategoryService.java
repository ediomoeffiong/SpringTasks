package com.fifthlab.springtasks.service;

import com.fifthlab.springtasks.model.Category;
import com.fifthlab.springtasks.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import org.springframework.lang.NonNull;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class CategoryService {
    private final CategoryRepository categoryRepository;

    private String getCurrentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            return auth.getName();
        }
        return "defaultUser";
    }

    public List<Category> findAll() {
        String username = getCurrentUsername();
        return categoryRepository.findAll().stream()
                .filter(c -> username.equals(c.getUsername()))
                .toList(); 
    }

    public Optional<Category> findById(@NonNull Long id) {
        return categoryRepository.findById(id);
    }

    public Category save(Category category) {
        if (category.getUsername() == null) {
            category.setUsername(getCurrentUsername());
        }
        if (category.getId() != null) {
            findById(category.getId()).ifPresent(existing -> {
                existing.setName(category.getName());
                existing.setDescription(category.getDescription());
                existing.setColor(category.getColor());
                categoryRepository.save(existing);
            });
            return category;
        } else {
            return categoryRepository.save(category);
        }
    }

    public void deleteById(@NonNull Long id) {
        categoryRepository.deleteById(id);
    }
}
