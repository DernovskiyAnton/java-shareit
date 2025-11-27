package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ItemDto(
        Long id,

        @NotBlank(message = "Name cannot be empty")
        String name,

        @NotBlank(message = "Description cannot be empty")
        String description,

        @NotNull(message = "Available status must be specified")
        Boolean available,

        Long requestId
) {
}