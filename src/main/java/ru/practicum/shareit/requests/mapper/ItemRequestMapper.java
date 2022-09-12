package ru.practicum.shareit.requests.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.stream.Collectors;

@Component
public class ItemRequestMapper {
    private final ItemMapper itemMapper;

    @Autowired
    public ItemRequestMapper(ItemMapper itemMapper) {
        this.itemMapper = itemMapper;
    }

    public ItemRequest toItemRequest(ItemRequestDto itemRequestDto, User ownerId) {
        return new ItemRequest(
                itemRequestDto.getDescription(),
                ownerId
        );
    }

    public ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        return new ItemRequestDto(
                itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getCreated(),
                itemRequest.getOwner().getId(),
                itemRequest.getItems()
                        .stream()
                        .map(itemMapper::toItemDto)
                        .collect(Collectors.toList())
        );
    }
}