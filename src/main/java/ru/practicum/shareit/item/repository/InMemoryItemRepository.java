package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class InMemoryItemRepository {

    private final Map<Long, Item> items = new HashMap<>();
    private Long currentId = 1L;

    public Item save(Item item) {
        if (item.getId() == null) {
            item.setId(currentId++);
        }
        items.put(item.getId(), item);
        return item;
    }

    public Item update(Item item) {
        items.put(item.getId(), item);
        return item;
    }

    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    public List<Item> findAll() {
        return new ArrayList<>(items.values());
    }

    public List<Item> findByOwnerId(Long ownerId) {
        return items.values().stream()
                .filter(item -> item.getOwner().getId().equals(ownerId))
                .collect(Collectors.toList());
    }

    public List<Item> searchByText(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        String lowerCaseText = text.toLowerCase();
        return items.values().stream()
                .filter(Item::getAvailable)
                .filter(item -> item.getName().toLowerCase().contains(lowerCaseText) ||
                        item.getDescription().toLowerCase().contains(lowerCaseText))
                .collect(Collectors.toList());
    }

    public void deleteById(Long id) {
        items.remove(id);
    }
}