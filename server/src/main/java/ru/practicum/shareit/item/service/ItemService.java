package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.comment.dto.CommentCreateDto;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {

    ItemDto create(Long userId, ItemCreateDto itemCreateDto);

    ItemDto update(Long userId, ItemDto itemDto, Long itemId);

    void deleteItem(Long itemId, Long userId);

    Item getById(Long itemId);

    ItemDto getById(Long itemId, Long userId);

    List<ItemDto> getUserItems(Long userId, Integer from, Integer size);

    List<ItemDto> keywordSearch(String keyword, Integer from, Integer size);

    CommentDto createComment(Long itemId, Long userId, CommentCreateDto commentCreateDto);
}