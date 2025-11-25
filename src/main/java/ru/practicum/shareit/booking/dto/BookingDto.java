package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import ru.practicum.shareit.booking.Status;

import java.time.LocalDateTime;

public record BookingDto(
        Long id,

        @NotNull(message = "Start date must be specified")
        @FutureOrPresent(message = "Start date must be in present or future")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime start,

        @NotNull(message = "End date must be specified")
        @Future(message = "End date must be in future")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime end,

        Long itemId,
        Item item,
        Booker booker,
        Status status
) {
    public record Item(Long id, String name) {
    }

    public record Booker(Long id, String name) {
    }
}