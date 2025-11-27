package ru.practicum.shareit.request.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class ItemRequestMapper {

    public static ItemRequestDto toItemRequestDto(ItemRequest request, List<Item> items) {
        if (request == null) {
            return null;
        }

        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(request.getId());
        dto.setDescription(request.getDescription());
        dto.setCreated(request.getCreated());

        List<ItemRequestDto.ItemDto> itemDtos = items != null
                ? items.stream()
                .map(item -> new ItemRequestDto.ItemDto(
                        item.getId(),
                        item.getName(),
                        item.getOwner().getId()))
                .collect(Collectors.toList())
                : List.of();

        dto.setItems(itemDtos);

        return dto;
    }

    public static ItemRequest toItemRequest(ItemRequestDto dto) {
        if (dto == null) {
            return null;
        }

        ItemRequest request = new ItemRequest();
        request.setId(dto.getId());
        request.setDescription(dto.getDescription());
        request.setCreated(dto.getCreated());

        return request;
    }
}