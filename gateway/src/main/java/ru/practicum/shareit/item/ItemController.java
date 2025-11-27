package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

/**
 * Gateway контроллер для работы с вещами.
 * Валидирует входные данные и проксирует запросы в Server.
 */
@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {

    private final ItemClient itemClient;

    /**
     * Добавление новой вещи.
     * POST /items
     */
    @PostMapping
    public ResponseEntity<Object> addItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                          @Valid @RequestBody ItemDto itemDto) {
        log.info("Gateway: Adding item for user {}", userId);
        return itemClient.addItem(userId, itemDto);
    }

    /**
     * Обновление вещи.
     * PATCH /items/{itemId}
     */
    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @PathVariable Long itemId,
                                             @RequestBody ItemDto itemDto) {
        log.info("Gateway: Updating item {} by user {}", itemId, userId);
        return itemClient.updateItem(userId, itemId, itemDto);
    }

    /**
     * Получение вещи по ID.
     * GET /items/{itemId}
     */
    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@PathVariable Long itemId,
                                              @RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId) {
        log.info("Gateway: Getting item {} by user {}", itemId, userId);
        return itemClient.getItemById(itemId, userId);
    }

    /**
     * Получение вещей владельца.
     * GET /items
     */
    @GetMapping
    public ResponseEntity<Object> getItemsByOwner(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Gateway: Getting items for owner {}", userId);
        return itemClient.getItemsByOwner(userId);
    }

    /**
     * Поиск вещей.
     * GET /items/search?text={text}
     */
    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(@RequestParam String text) {
        log.info("Gateway: Searching items with text: {}", text);
        return itemClient.searchItems(text);
    }

    /**
     * Добавление комментария.
     * POST /items/{itemId}/comment
     */
    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @PathVariable Long itemId,
                                             @Valid @RequestBody CommentDto commentDto) {
        log.info("Gateway: Adding comment for item {} by user {}", itemId, userId);
        return itemClient.addComment(userId, itemId, commentDto);
    }
}