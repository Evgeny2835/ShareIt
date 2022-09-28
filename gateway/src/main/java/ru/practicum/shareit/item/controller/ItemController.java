package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.client.ItemClient;
import ru.practicum.shareit.item.comment.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") @Positive long userId,
                                         @RequestBody @Valid ItemCreateDto itemCreateDto) {
        log.info("Creating item {}", itemCreateDto);
        return itemClient.create(userId, itemCreateDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader("X-Sharer-User-Id") @Positive long userId,
                                         @PathVariable("itemId") @Positive long itemId,
                                         @RequestBody ItemDto itemDto) {
        log.info("Update item, itemId={}, userId={}", itemId, userId);
        return itemClient.update(itemId, userId, itemDto);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Object> delete(@RequestHeader("X-Sharer-User-Id") @Positive long userId,
                                         @PathVariable("itemId") @Positive long itemId) {
        log.info("Delete item, itemId={}, userId={}", itemId, userId);
        return itemClient.delete(itemId, userId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> get(@RequestHeader("X-Sharer-User-Id") @Positive long userId,
                                      @PathVariable("itemId") @Positive long itemId) {
        log.info("Get item, itemId={}, userId={}", itemId, userId);
        return itemClient.get(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getUserItems(
            @RequestHeader("X-Sharer-User-Id") @Positive long userId,
            @RequestParam(name = "from", defaultValue =  "0") @PositiveOrZero Integer from,
            @RequestParam(name = "size", defaultValue = "10") @Positive Integer size) {
        log.info("Get all user items, userId={}, from={}, size={}", userId, from, size);
        return itemClient.getUserItems(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> keywordSearch(
            @RequestParam(name = "text", defaultValue = "") String keyword,
            @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(name = "size", defaultValue = "10") @Positive Integer size) {
        if (keyword.isEmpty()) {
            return ResponseEntity.ok().body("[]");
        }
        return itemClient.keywordSearch(keyword, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(
            @RequestHeader("X-Sharer-User-Id") @Positive long userId,
            @RequestBody @Valid CommentCreateDto commentCreateDto,
            @PathVariable @Positive long itemId) {
        return itemClient.createComment(itemId, userId, commentCreateDto);
    }
}