package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {

    ItemDto create(Long userId, ItemDto itemDto);

    ItemDto update(Long userId, ItemDto itemDto, Long itemId);

    void deleteItem(Long userId, Long itemId);

    Item getById(Long id);

    List<ItemDto> getUserItems(Long userId, Integer from, Integer size);

    List<ItemDto> keywordSearch(String keyword, Integer from, Integer size);

    Comment createComment(Long userId, Long itemId, CommentDto commentDto);
}