package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

public record ItemWithBookingsDto(
        Long id,
        String name,
        String description,
        Boolean available,
        BookingShortDto lastBooking,
        BookingShortDto nextBooking,
        List<CommentDto> comments
) {
    public record BookingShortDto(
            Long id,
            Long bookerId,
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            LocalDateTime start,
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            LocalDateTime end
    ) {
    }

    public record CommentDto(
            Long id,
            String text,
            String authorName,
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
            LocalDateTime created
    ) {
    }
}