package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.Status;

import java.time.LocalDateTime;

public record BookingDto(
        Long id,
        LocalDateTime start,
        LocalDateTime end,
        Long itemId,
        Long bookerId,
        Status status
) {
}