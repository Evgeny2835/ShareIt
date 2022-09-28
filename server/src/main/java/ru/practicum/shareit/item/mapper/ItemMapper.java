package ru.practicum.shareit.item.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.requests.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;

@Component
public class ItemMapper {
    private final ItemRequestRepository itemRequestRepository;

    public ItemMapper(ItemRequestRepository itemRequestRepository) {
        this.itemRequestRepository = itemRequestRepository;
    }

    public Item toItem(ItemCreateDto itemCreateDto, User owner) {
        return new Item(
                itemCreateDto.getName(),
                itemCreateDto.getDescription(),
                itemCreateDto.getAvailable(),
                owner,
                itemCreateDto.getRequestId() != null ? itemRequestRepository
                        .findById(itemCreateDto.getRequestId()).orElse(null) : null
        );
    }

    public ItemDto toItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .ownerId(item.getOwner().getId())
                .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                .build();
    }

    public BookingItemDto toBookingItemDto(Booking booking) {
        return BookingItemDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .bookerId(booking.getBooker().getId())
                .build();
    }
}