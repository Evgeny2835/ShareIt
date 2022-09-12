package ru.practicum.shareit.item.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.service.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
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
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;

    @Autowired
    public ItemController(ItemService itemService, ItemMapper itemMapper, CommentMapper commentMapper) {
        this.itemService = itemService;
        this.itemMapper = itemMapper;
        this.commentMapper = commentMapper;
    }

    @PostMapping
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                          @Valid @RequestBody ItemDto itemDto) {
        return itemService.create(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                          @RequestBody ItemDto itemDto,
                          @PathVariable("itemId") @Positive long itemId) {
        return itemService.update(userId, itemDto, itemId);
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                           @PathVariable("itemId") @Positive long itemId) {
        itemService.deleteItem(userId, itemId);
    }

    @GetMapping("/{itemId}")
    public ItemDto get(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                       @PathVariable("itemId") @Positive long itemId) {
        return itemMapper.toItemDto(itemService.getById(itemId), userId);
    }

    @GetMapping
    public List<ItemDto> getUserItems(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                      @RequestParam(name = "from", required = false) @PositiveOrZero Integer from,
                                      @RequestParam(name = "size", required = false) @Positive Integer size) {
        return itemService.getUserItems(userId, from, size);
    }

    @GetMapping("/search")
    public List<ItemDto> keywordSearch(@RequestParam(name = "text", defaultValue = "") String keyword,
                                       @RequestParam(name = "from", required = false) @PositiveOrZero Integer from,
                                       @RequestParam(name = "size", required = false) @Positive Integer size) {
        if (keyword.isEmpty()) {
            return Collections.emptyList();
        }
        return itemService.keywordSearch(keyword, from, size);
    }

    @PostMapping("{itemId}/comment")
    public CommentDto createComment(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                    @Valid @RequestBody CommentDto commentDto,
                                    @PathVariable @Positive Long itemId) {
        return commentMapper.toCommentDto(itemService.createComment(userId, itemId, commentDto));
    }
}