package com.axelbon.pos.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.axelbon.pos.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
    
    // SELECT * FROM categories WHERE name = ? AND id != ?
    Optional<Category> findByNameAndIdNot(String name, Long id);
}
