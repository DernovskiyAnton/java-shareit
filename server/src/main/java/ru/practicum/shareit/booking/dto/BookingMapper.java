package ru.practicum.shareit.booking.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.Booking;

@UtilityClass
public class BookingMapper {

    public static BookingDto toBookingDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        return new BookingDto(
                booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getItem() != null ? booking.getItem().getId() : null,
                booking.getItem() != null ? new BookingDto.Item(
                        booking.getItem().getId(),
                        booking.getItem().getName()
                ) : null,
                booking.getBooker() != null ? new BookingDto.Booker(
                        booking.getBooker().getId(),
                        booking.getBooker().getName()
                ) : null,
                booking.getStatus()
        );
    }

    public static Booking toBooking(BookingDto bookingDto) {
        if (bookingDto == null) {
            return null;
        }
        Booking booking = new Booking();
        booking.setId(bookingDto.id());
        booking.setStart(bookingDto.start());
        booking.setEnd(bookingDto.end());
        booking.setStatus(bookingDto.status());
        return booking;
    }
}