package ru.practicum.shareit.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.List;

public record ItemRequestDto(
        Long id,

        @NotBlank(message = "Description cannot be empty")
        String description,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime created,

        List<ItemDto> items
) {
    public record ItemDto(
            Long id,
            String name,
            String description,
            Boolean available,
            Long requestId
    ) {
    }
}