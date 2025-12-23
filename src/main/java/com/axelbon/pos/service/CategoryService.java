package com.axelbon.pos.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.axelbon.pos.dto.category.CategoryFilterDTO;
import com.axelbon.pos.dto.category.CategoryResponseDTO;
import com.axelbon.pos.dto.category.CreateCategoryDTO;
import com.axelbon.pos.dto.category.UpdateCategoryDTO;

public interface CategoryService {
    // findAll category
    Page<CategoryResponseDTO> findAll(Pageable pageable, CategoryFilterDTO searchFilters);

    // findById category
    CategoryResponseDTO findById(Long id);

    // save category
    CategoryResponseDTO saveCategory(CreateCategoryDTO category, String reason);

    // update category
    CategoryResponseDTO updateCategory(Long id, UpdateCategoryDTO category, String reason);

    // delete category
    void deleteCategory(Long id, String reason);
}
