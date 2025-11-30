package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import lombok.experimental.UtilityClass;

import java.util.List;

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

    public static ItemWithBookingsDto toItemWithBookingsDto(
            Item item,
            Booking lastBooking,
            Booking nextBooking,
            List<Comment> comments) {
        if (item == null) {
            return null;
        }

        ItemWithBookingsDto.BookingShortDto lastBookingDto = lastBooking != null
                ? new ItemWithBookingsDto.BookingShortDto(
                lastBooking.getId(),
                lastBooking.getBooker().getId(),
                lastBooking.getStart(),
                lastBooking.getEnd())
                : null;

        ItemWithBookingsDto.BookingShortDto nextBookingDto = nextBooking != null
                ? new ItemWithBookingsDto.BookingShortDto(
                nextBooking.getId(),
                nextBooking.getBooker().getId(),
                nextBooking.getStart(),
                nextBooking.getEnd())
                : null;

        List<ItemWithBookingsDto.CommentDto> commentDtos = comments != null
                ? comments.stream()
                .map(comment -> new ItemWithBookingsDto.CommentDto(
                        comment.getId(),
                        comment.getText(),
                        comment.getAuthor().getName(),
                        comment.getCreated()))
                .toList()
                : List.of();

        return new ItemWithBookingsDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                lastBookingDto,
                nextBookingDto,
                commentDtos
        );
    }

    public static CommentDto toCommentDto(Comment comment) {
        if (comment == null) {
            return null;
        }
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                comment.getAuthor().getName(),
                comment.getCreated()
        );
    }

    public static Comment toComment(CommentDto commentDto) {
        if (commentDto == null) {
            return null;
        }
        Comment comment = new Comment();
        comment.setId(commentDto.id());
        comment.setText(commentDto.text());
        comment.setCreated(commentDto.created());
        return comment;
    }
}