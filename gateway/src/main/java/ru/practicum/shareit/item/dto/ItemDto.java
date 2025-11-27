package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Gateway DTO для Item с полной валидацией входных данных.
 * Проверяет обязательные поля перед отправкой в Server.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {

    private Long id;

    @NotBlank(message = "Name cannot be empty")
    private String name;

    @NotBlank(message = "Description cannot be empty")
    private String description;

    @NotNull(message = "Available status must be specified")
    private Boolean available;

    // requestId необязательное - можно создавать items без запроса
    private Long requestId;
}
