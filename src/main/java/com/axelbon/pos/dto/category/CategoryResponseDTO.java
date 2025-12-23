package com.axelbon.pos.dto.category;

import java.time.LocalDateTime;

public record CategoryResponseDTO(
    Long id,
    String name,
    String description,
    LocalDateTime createdAt
) {

}
