package ru.practicum.shareit.item.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.dto.CommentCreateDto;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/items")
@Validated
public class ItemController {
    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto create(
            @RequestHeader("X-Sharer-User-Id") @Positive long userId,
            @RequestBody @Valid ItemCreateDto itemCreateDto) {
        return itemService.create(userId, itemCreateDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(
            @RequestHeader("X-Sharer-User-Id") @Positive long userId,
            @RequestBody ItemDto itemDto,
            @PathVariable("itemId") @Positive long itemId) {
        return itemService.update(userId, itemDto, itemId);
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(
            @RequestHeader("X-Sharer-User-Id") @Positive long userId,
            @PathVariable("itemId") @Positive long itemId) {
        itemService.deleteItem(itemId, userId);
    }

    @GetMapping("/{itemId}")
    public ItemDto get(
            @RequestHeader("X-Sharer-User-Id") @Positive long userId,
            @PathVariable("itemId") @Positive long itemId) {
        return itemService.getById(itemId, userId);
    }

    @GetMapping
    public List<ItemDto> getUserItems(
            @RequestHeader("X-Sharer-User-Id") @Positive long userId,
            @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(name = "size", defaultValue = "10") @Positive Integer size) {
        return itemService.getUserItems(userId, from, size);
    }

    @GetMapping("/search")
    public List<ItemDto> keywordSearch(
            @RequestParam(name = "text", defaultValue = "") String keyword,
            @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(name = "size", defaultValue = "10") @Positive Integer size) {
        if (keyword.isEmpty()) {
            return Collections.emptyList();
        }
        return itemService.keywordSearch(keyword, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(
            @RequestHeader("X-Sharer-User-Id") @Positive long userId,
            @RequestBody @Valid CommentCreateDto commentCreateDto,
            @PathVariable("itemId") @Positive long itemId) {
        return itemService.createComment(itemId, userId, commentCreateDto);
    }
}