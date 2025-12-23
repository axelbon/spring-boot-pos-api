package com.axelbon.pos.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.axelbon.pos.dto.category.CategoryFilterDTO;
import com.axelbon.pos.dto.category.CategoryResponseDTO;
import com.axelbon.pos.dto.category.CreateCategoryDTO;
import com.axelbon.pos.dto.category.UpdateCategoryDTO;
import com.axelbon.pos.entity.AuditLog;
import com.axelbon.pos.entity.Category;
import com.axelbon.pos.entity.enums.AuditAction;
import com.axelbon.pos.repository.CategoryRepository;
import com.axelbon.pos.service.AuditLogService;
import com.axelbon.pos.service.CategoryService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService{

    private final CategoryRepository categoryRepository;

    private final AuditLogService auditLogService;

    @Override
    public Page<CategoryResponseDTO> findAll(Pageable pageable, CategoryFilterDTO searchFilters) {
        return categoryRepository.findAll(pageable).map(this::mapToDto);
    }

    @Override
    public CategoryResponseDTO findById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
            //TODO: Implementar GlobalExceptionHandler
            .orElseThrow(() -> new Error("Not found: Category not found with `category_id` provided`."));

        return mapToDto(category);
    }

    @Override
    @Transactional
    public CategoryResponseDTO saveCategory(CreateCategoryDTO createCategoryDto, String reason) {
        if(categoryRepository.findByName(createCategoryDto.name()).isPresent()){
            //TODO: Implementar GlobalExceptionHandler
            throw new Error("Conflict: Category with provided `name` already exists.");
        }

        Category tempCategory = new Category();
        tempCategory.setName(createCategoryDto.name());
        tempCategory.setDescription(createCategoryDto.description());

        Category newCategory = categoryRepository.save(tempCategory);
        
        //TODO: Obtener id desde usuario autenticado con JWT
        Long auditorId = 1L;
        AuditLog auditLog = new AuditLog();
        auditLog.setAuditorId(auditorId);
        auditLog.setReason(reason);
        auditLog.setAction(AuditAction.CREATE);
        auditLog.setCategoryId(newCategory.getId());

        auditLogService.saveAuditLog(auditLog);

        return mapToDto(newCategory);
    }

    @Override
    @Transactional
    public CategoryResponseDTO updateCategory(Long categoryId, UpdateCategoryDTO updateCategoryDto, String reason) {
        Category category = categoryRepository.findById(categoryId)
            //TODO: Implementar GlobalExceptionHandler
            .orElseThrow(() -> new Error("Not found: Category not found with provided `category_id`."));

        categoryRepository.findByNameAndIdNot(updateCategoryDto.name(), categoryId)
            //TODO: Implementar GlobalExceptionHandler
            .ifPresent(conflictingCategory -> {
                throw new Error("Conflict: Category with provided `name` already exists.");
            });

        category.setName(updateCategoryDto.name());
        category.setDescription(updateCategoryDto.description());
        
        //TODO: Obtener id desde usuario autenticado con JWT
        Long auditorId = 1L;
        AuditLog auditLog = new AuditLog();
        auditLog.setAuditorId(auditorId);
        auditLog.setCategoryId(category.getId());
        auditLog.setAction(AuditAction.UPDATE);
        auditLog.setReason(reason);

        auditLogService.saveAuditLog(auditLog);

        return mapToDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId, String reason) {
        //TODO: Implementar GlobalExceptionHandler
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new Error("Not found: Category not found with provided `category_id`."));

        //TODO: Obtener id desde usuario autenticado con JWT
        Long auditorId = 1L;
        AuditLog auditLog = new AuditLog();
        auditLog.setAuditorId(auditorId);
        auditLog.setCategoryId(categoryId);
        auditLog.setAction(AuditAction.DELETE);
        auditLog.setReason(reason);

        auditLogService.saveAuditLog(auditLog);

        categoryRepository.delete(category);
    }

    // TODO: Implement MapStruct
    private CategoryResponseDTO mapToDto(Category category){
        return new CategoryResponseDTO(
            category.getId(),
            category.getName(),
            category.getDescription(),
            category.getCreatedAt()
        );
    }
}
