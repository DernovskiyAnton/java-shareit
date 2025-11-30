package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

/**
 * Контроллер для работы с вещами (Server модуль).
 * Валидация данных происходит в Gateway, поэтому здесь @Valid не используется.
 */
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    /**
     * Добавить новую вещь.
     * POST /items
     *
     * @param userId  ID владельца из заголовка X-Sharer-User-Id
     * @param itemDto данные новой вещи (уже провалидированы в Gateway)
     * @return созданная вещь
     */
    @PostMapping
    public ItemDto addItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                           @RequestBody ItemDto itemDto) {
        return itemService.addItem(userId, itemDto);
    }

    /**
     * Обновить данные вещи.
     * PATCH /items/{itemId}
     *
     * @param userId  ID пользователя из заголовка X-Sharer-User-Id
     * @param itemId  ID вещи для обновления
     * @param itemDto новые данные (могут быть частичными)
     * @return обновлённая вещь
     */
    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @PathVariable Long itemId,
                              @RequestBody ItemDto itemDto) {
        return itemService.updateItem(userId, itemId, itemDto);
    }

    /**
     * Получить данные о конкретной вещи.
     * GET /items/{itemId}
     *
     * Если запрашивает владелец - возвращаются даты бронирований.
     * Если запрашивает не владелец - бронирования не показываются.
     *
     * @param itemId ID вещи
     * @param userId ID пользователя (может быть null)
     * @return данные вещи с комментариями и бронированиями (для владельца)
     */
    @GetMapping("/{itemId}")
    public ItemWithBookingsDto getItemById(@PathVariable Long itemId,
                                           @RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId) {
        return itemService.getItemById(itemId, userId);
    }

    /**
     * Получить список всех вещей владельца.
     * GET /items
     *
     * Для каждой вещи возвращаются даты последнего и следующего бронирования.
     *
     * @param userId ID владельца из заголовка X-Sharer-User-Id
     * @return список вещей владельца с бронированиями
     */
    @GetMapping
    public List<ItemWithBookingsDto> getItemsByOwner(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getItemsByOwner(userId);
    }

    /**
     * Поиск вещей по тексту.
     * GET /items/search?text={text}
     *
     * Ищет текст в названии или описании вещи.
     * Возвращает только доступные для аренды вещи.
     *
     * @param text текст для поиска
     * @return список найденных вещей
     */
    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text) {
        return itemService.searchItems(text);
    }

    /**
     * Добавить комментарий к вещи.
     * POST /items/{itemId}/comment
     *
     * Комментарий может оставить только пользователь, который брал вещь в аренду
     * и аренда уже завершилась.
     *
     * @param userId     ID автора комментария из заголовка X-Sharer-User-Id
     * @param itemId     ID вещи для комментирования
     * @param commentDto данные комментария (уже провалидированы в Gateway)
     * @return созданный комментарий
     */
    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @PathVariable Long itemId,
                                 @RequestBody CommentDto commentDto) {
        return itemService.addComment(userId, itemId, commentDto);
    }
}