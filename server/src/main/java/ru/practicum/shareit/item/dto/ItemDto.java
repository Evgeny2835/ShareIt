package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.item.comment.dto.CommentDto;

import java.util.Set;

@Data
@Builder
public class ItemDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long ownerId;
    private Long requestId;
    private BookingItemDto lastBooking;
    private BookingItemDto nextBooking;
    private Set<CommentDto> comments;
}