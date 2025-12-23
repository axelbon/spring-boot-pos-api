package com.axelbon.pos.dto.category;

import jakarta.validation.constraints.NotBlank;

public record UpdateCategoryDTO(
    @NotBlank(message = "Name can't be empty.")
    String name,
    @NotBlank(message = "Description can't be empty.")
    String description
) {
    
}
