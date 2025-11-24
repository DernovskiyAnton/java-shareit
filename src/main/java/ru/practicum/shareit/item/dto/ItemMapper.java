package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.model.Item;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ItemMapper {


    public static ItemDto toItemDto(Item item) {
        if (item == null) {
            return null;
        }
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequest()
        );
    }

    public static Item toItem(ItemDto itemDto) {
        if (itemDto == null) {
            return null;
        }
        Item item = new Item();
        item.setId(itemDto.id());
        item.setName(itemDto.name());
        item.setDescription(itemDto.description());
        item.setAvailable(itemDto.available());
        item.setRequest(itemDto.requestId());
        return item;
    }

    public static Item updateItemFromDto(Item item, ItemDto itemDto) {
        if (itemDto.name() != null && !itemDto.name().isBlank()) {
            item.setName(itemDto.name());
        }
        if (itemDto.description() != null && !itemDto.description().isBlank()) {
            item.setDescription(itemDto.description());
        }
        if (itemDto.available() != null) {
            item.setAvailable(itemDto.available());
        }
        return item;
    }
}