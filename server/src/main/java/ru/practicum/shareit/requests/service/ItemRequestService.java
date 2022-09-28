package ru.practicum.shareit.requests.service;

import ru.practicum.shareit.requests.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto create(Long ownerId, ItemRequestDto dto);

    List<ItemRequestDto> getByUser(Long userId);

    List<ItemRequestDto> getAll(Long userId, Integer from, Integer size);

    ItemRequestDto getById(Long ownerId, Long requestId);
}