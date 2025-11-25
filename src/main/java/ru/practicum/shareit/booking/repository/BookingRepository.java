package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.Status;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Все бронирования пользователя (как арендатора)
    List<Booking> findByBookerId(Long bookerId, Sort sort);

    // Текущие бронирования пользователя
    List<Booking> findByBookerIdAndStartBeforeAndEndAfter(
            Long bookerId,
            LocalDateTime start,
            LocalDateTime end,
            Sort sort
    );

    // Прошедшие бронирования пользователя
    List<Booking> findByBookerIdAndEndBefore(Long bookerId, LocalDateTime end, Sort sort);

    // Будущие бронирования пользователя
    List<Booking> findByBookerIdAndStartAfter(Long bookerId, LocalDateTime start, Sort sort);

    // Бронирования пользователя по статусу
    List<Booking> findByBookerIdAndStatus(Long bookerId, Status status, Sort sort);

    // Все бронирования для вещей владельца
    @Query("select b from Booking b " +
            "where b.item.owner.id = ?1 " +
            "order by b.start desc")
    List<Booking> findByItemOwnerId(Long ownerId);

    // Текущие бронирования для вещей владельца
    @Query("select b from Booking b " +
            "where b.item.owner.id = ?1 " +
            "and b.start < ?2 " +
            "and b.end > ?2 " +
            "order by b.start desc")
    List<Booking> findCurrentBookingsByItemOwnerId(Long ownerId, LocalDateTime now);

    // Прошедшие бронирования для вещей владельца
    @Query("select b from Booking b " +
            "where b.item.owner.id = ?1 " +
            "and b.end < ?2 " +
            "order by b.start desc")
    List<Booking> findPastBookingsByItemOwnerId(Long ownerId, LocalDateTime now);

    // Будущие бронирования для вещей владельца
    @Query("select b from Booking b " +
            "where b.item.owner.id = ?1 " +
            "and b.start > ?2 " +
            "order by b.start desc")
    List<Booking> findFutureBookingsByItemOwnerId(Long ownerId, LocalDateTime now);

    // Бронирования для вещей владельца по статусу
    @Query("select b from Booking b " +
            "where b.item.owner.id = ?1 " +
            "and b.status = ?2 " +
            "order by b.start desc")
    List<Booking> findByItemOwnerIdAndStatus(Long ownerId, Status status);

    // Бронирования для конкретной вещи
    List<Booking> findByItemId(Long itemId, Sort sort);

    // Последнее завершённое бронирование для вещи
    @Query("select b from Booking b " +
            "where b.item.id = ?1 " +
            "and b.start < ?2 " +
            "and b.status = 'APPROVED' " +
            "order by b.end desc " +
            "limit 1")
    Booking findLastBookingForItem(Long itemId, LocalDateTime now);

    // Ближайшее следующее бронирование для вещи
    @Query("select b from Booking b " +
            "where b.item.id = ?1 " +
            "and b.start > ?2 " +
            "and b.status = 'APPROVED' " +
            "order by b.start asc " +
            "limit 1")
    Booking findNextBookingForItem(Long itemId, LocalDateTime now);
}