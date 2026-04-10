package com.fifthlab.springtasks.repository;

import com.fifthlab.springtasks.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByNameAndUsername(String name, String username);
}
