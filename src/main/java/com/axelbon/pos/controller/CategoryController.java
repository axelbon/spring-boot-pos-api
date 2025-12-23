package com.axelbon.pos.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.axelbon.pos.dto.category.CategoryFilterDTO;
import com.axelbon.pos.dto.category.CategoryResponseDTO;
import com.axelbon.pos.dto.category.CreateCategoryDTO;
import com.axelbon.pos.dto.category.UpdateCategoryDTO;
import com.axelbon.pos.service.CategoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    
    private final CategoryService categoryService;

    @GetMapping
    public Page<CategoryResponseDTO> getAll(CategoryFilterDTO filters, Pageable pageable) {
        return categoryService.findAll(pageable, filters);
    }
    
    @GetMapping("/{id}")
    public CategoryResponseDTO findById(@PathVariable Long id) {
        return categoryService.findById(id);
    }
    
    @PostMapping()
    public CategoryResponseDTO saveCategory(@RequestBody CreateCategoryDTO category, @RequestHeader("X-AUDIT-REASON") String reason) {
        return categoryService.saveCategory(category, reason);
    }

    @PutMapping("/{id}")
    public CategoryResponseDTO updateCategory(@PathVariable Long id, @RequestBody UpdateCategoryDTO category, @RequestHeader("X-AUDIT-REASON") String reason) {
        return categoryService.updateCategory(id, category, reason);
    }

    @DeleteMapping("/{id}")
    public void deleteCategory(@PathVariable Long id, @RequestHeader("X-AUDIT-REASON") String reason){
        categoryService.deleteCategory(id, reason);
    }
    
}
