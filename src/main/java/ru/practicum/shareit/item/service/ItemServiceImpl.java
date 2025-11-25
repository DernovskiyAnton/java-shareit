package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.InMemoryItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.InMemoryUserRepository;

import java.util.List;

/**
 * Реализация сервиса для работы с вещами.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final InMemoryItemRepository itemRepository;
    private final InMemoryUserRepository userRepository;

    @Override
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
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        log.debug("Updating item with id: {} by user with id: {}", itemId, userId);
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item with id=" + itemId + " not found"));

        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new NotFoundException("User with id=" + userId + " is not the owner of the item");
        }

        Item updatedItem = ItemMapper.updateItemFromDto(existingItem, itemDto);
        updatedItem = itemRepository.update(updatedItem);

        log.info("Item updated with id: {}", itemId);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        log.debug("Getting item by id: {}", itemId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item with id=" + itemId + " not found"));
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getItemsByOwner(Long userId) {
        log.debug("Getting items for user with id: {}", userId);
        if (userRepository.findById(userId).isEmpty()) {
            throw new NotFoundException("User with id=" + userId + " not found");
        }

        return itemRepository.findByOwnerId(userId).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        log.debug("Searching items with text: {}", text);
        return itemRepository.searchByText(text).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }
}