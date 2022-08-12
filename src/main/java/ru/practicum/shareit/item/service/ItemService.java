package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;

public interface ItemService {

    ItemDto add(Long userId, ItemDto itemDto);

    ItemDto update(Long userId, ItemDto itemDto, Long itemId);

    void deleteItem(Long userId, Long itemId);

    ItemDto getById(Long id);

    Collection<ItemDto> getUserItems(Long userId);

    Collection<ItemDto> keywordSearch(String keyword);
}