package ru.practicum.shareit.item.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.Collection;
import java.util.Collections;

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
    public ItemDto add(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                       @Valid @RequestBody ItemDto itemDto) {
        return itemService.add(userId, itemDto);
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
    public ItemDto get(@PathVariable("itemId") @Positive long itemId) {
        return itemService.getById(itemId);
    }

    @GetMapping
    public Collection<ItemDto> getUserItems(@RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        return itemService.getUserItems(userId);
    }

    @GetMapping("/search")
    public Collection<ItemDto> keywordSearch(@RequestParam(name = "text", defaultValue = "") String keyword) {
        if (keyword.isEmpty()) {
            return Collections.emptyList();
        }
        return itemService.keywordSearch(keyword);
    }
}