package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Реализация сервиса для работы с вещами.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto addItem(Long userId, ItemDto itemDto) {
        log.debug("Adding item for user with id: {}", userId);
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " not found"));

        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);
        item = itemRepository.save(item);

        log.info("Item created with id: {}", item.getId());
        return ItemMapper.toItemDto(item);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        log.debug("Updating item with id: {} by user with id: {}", itemId, userId);
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item with id=" + itemId + " not found"));

        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new NotFoundException("User with id=" + userId + " is not the owner of the item");
        }

        Item updatedItem = ItemMapper.updateItemFromDto(existingItem, itemDto);
        updatedItem = itemRepository.save(updatedItem);

        log.info("Item updated with id: {}", itemId);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemWithBookingsDto getItemById(Long itemId, Long userId) {
        log.debug("Getting item by id: {}", itemId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item with id=" + itemId + " not found"));

        // Бронирования видны только владельцу
        Booking lastBooking = null;
        Booking nextBooking = null;
        if (userId != null && item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();
            lastBooking = bookingRepository.findLastBookingForItem(itemId, now);
            nextBooking = bookingRepository.findNextBookingForItem(itemId, now);
        }

        List<Comment> comments = commentRepository.findByItemId(itemId);

        return ItemMapper.toItemWithBookingsDto(item, lastBooking, nextBooking, comments);
    }

    @Override
    public List<ItemWithBookingsDto> getItemsByOwner(Long userId) {
        log.debug("Getting items for user with id: {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " not found");
        }

        List<Item> items = itemRepository.findByOwnerId(userId);
        LocalDateTime now = LocalDateTime.now();

        return items.stream()
                .map(item -> {
                    Booking lastBooking = bookingRepository.findLastBookingForItem(item.getId(), now);
                    Booking nextBooking = bookingRepository.findNextBookingForItem(item.getId(), now);
                    List<Comment> comments = commentRepository.findByItemId(item.getId());
                    return ItemMapper.toItemWithBookingsDto(item, lastBooking, nextBooking, comments);
                })
                .toList();
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        log.debug("Searching items with text: {}", text);
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        log.debug("Adding comment for item {} by user {}", itemId, userId);

        // Проверка существования пользователя
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " not found"));

        // Проверка существования вещи
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item with id=" + itemId + " not found"));

        // Проверка что пользователь брал вещь в аренду и аренда завершилась
        LocalDateTime now = LocalDateTime.now();
        boolean hasBooking = bookingRepository.existsByBookerIdAndItemIdAndEndBeforeAndStatusApproved(
                userId, itemId, now);

        if (!hasBooking) {
            throw new BadRequestException(
                    "User with id=" + userId + " cannot comment item with id=" + itemId +
                            " without completed booking");
        }

        Comment comment = new Comment();
        comment.setText(commentDto.text());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        comment = commentRepository.save(comment);
        log.info("Comment created with id: {}", comment.getId());

        return ItemMapper.toCommentDto(comment);
    }
}