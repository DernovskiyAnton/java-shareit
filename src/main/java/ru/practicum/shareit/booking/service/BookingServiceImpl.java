package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.Status;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BookingDto createBooking(Long userId, BookingDto bookingDto) {
        log.debug("Creating booking for user {} and item {}", userId, bookingDto.itemId());

        // Валидация дат
        if (bookingDto.end().isBefore(bookingDto.start()) ||
                bookingDto.end().equals(bookingDto.start())) {
            throw new ConflictException("End date must be after start date");
        }

        // Проверка пользователя
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " not found"));

        // Проверка вещи
        Item item = itemRepository.findById(bookingDto.itemId())
                .orElseThrow(() -> new NotFoundException("Item with id=" + bookingDto.itemId() + " not found"));

        // Нельзя забронировать свою вещь
        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Owner cannot book his own item");
        }

        // Проверка доступности
        if (!item.getAvailable()) {
            throw new ConflictException("Item with id=" + bookingDto.itemId() + " is not available");
        }

        Booking booking = new Booking();
        booking.setStart(bookingDto.start());
        booking.setEnd(bookingDto.end());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(Status.WAITING);

        booking = bookingRepository.save(booking);
        log.info("Booking created with id: {}", booking.getId());

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    @Transactional
    public BookingDto approveBooking(Long userId, Long bookingId, Boolean approved) {
        log.debug("User {} approving booking {}: {}", userId, bookingId, approved);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking with id=" + bookingId + " not found"));

        // Только владелец вещи может подтвердить
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Only item owner can approve booking");
        }

        // Нельзя изменить уже подтверждённое/отклонённое бронирование
        if (!booking.getStatus().equals(Status.WAITING)) {
            throw new ConflictException("Booking is already " + booking.getStatus());
        }

        booking.setStatus(approved ? Status.APPROVED : Status.REJECTED);
        booking = bookingRepository.save(booking);

        log.info("Booking {} status changed to {}", bookingId, booking.getStatus());
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingDto getBookingById(Long userId, Long bookingId) {
        log.debug("Getting booking {} by user {}", bookingId, userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking with id=" + bookingId + " not found"));

        // Просмотр доступен только автору бронирования или владельцу вещи
        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("User with id=" + userId + " cannot view this booking");
        }

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getUserBookings(Long userId, BookingState state) {
        log.debug("Getting bookings for user {} with state {}", userId, state);

        // Проверка существования пользователя
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " not found");
        }

        LocalDateTime now = LocalDateTime.now();
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        List<Booking> bookings;

        switch (state) {
            case ALL:
                bookings = bookingRepository.findByBookerId(userId, sort);
                break;
            case CURRENT:
                bookings = bookingRepository.findByBookerIdAndStartBeforeAndEndAfter(
                        userId, now, now, sort);
                break;
            case PAST:
                bookings = bookingRepository.findByBookerIdAndEndBefore(userId, now, sort);
                break;
            case FUTURE:
                bookings = bookingRepository.findByBookerIdAndStartAfter(userId, now, sort);
                break;
            case WAITING:
                bookings = bookingRepository.findByBookerIdAndStatus(userId, Status.WAITING, sort);
                break;
            case REJECTED:
                bookings = bookingRepository.findByBookerIdAndStatus(userId, Status.REJECTED, sort);
                break;
            default:
                throw new IllegalArgumentException("Unknown state: " + state);
        }

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .toList();
    }

    @Override
    public List<BookingDto> getOwnerBookings(Long ownerId, BookingState state) {
        log.debug("Getting bookings for owner {} with state {}", ownerId, state);

        // Проверка существования пользователя
        if (!userRepository.existsById(ownerId)) {
            throw new NotFoundException("User with id=" + ownerId + " not found");
        }

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        switch (state) {
            case ALL:
                bookings = bookingRepository.findByItemOwnerId(ownerId);
                break;
            case CURRENT:
                bookings = bookingRepository.findCurrentBookingsByItemOwnerId(ownerId, now);
                break;
            case PAST:
                bookings = bookingRepository.findPastBookingsByItemOwnerId(ownerId, now);
                break;
            case FUTURE:
                bookings = bookingRepository.findFutureBookingsByItemOwnerId(ownerId, now);
                break;
            case WAITING:
                bookings = bookingRepository.findByItemOwnerIdAndStatus(ownerId, Status.WAITING);
                break;
            case REJECTED:
                bookings = bookingRepository.findByItemOwnerIdAndStatus(ownerId, Status.REJECTED);
                break;
            default:
                throw new IllegalArgumentException("Unknown state: " + state);
        }

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .toList();
    }
}