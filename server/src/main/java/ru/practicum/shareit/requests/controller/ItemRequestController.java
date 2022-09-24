package ru.practicum.shareit.requests.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.service.ItemRequestService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@Validated
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @Autowired
    public ItemRequestController(ItemRequestService itemRequestService) {
        this.itemRequestService = itemRequestService;
    }

    @PostMapping
    public ItemRequestDto create(
            @RequestHeader("X-Sharer-User-Id") @Positive Long ownerId,
            @Valid @RequestBody ItemRequestDto itemRequestDto) {
        return itemRequestService.create(ownerId, itemRequestDto);
    }

    @GetMapping
    public List<ItemRequestDto> getByUser(
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        return itemRequestService.getByUser(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAll(
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId,
            @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(name = "size", defaultValue = "10") @Positive Integer size) {
        return itemRequestService.getAll(userId, from, size);
    }

    @GetMapping("{requestId}")
    public ItemRequestDto getById(
            @RequestHeader("X-Sharer-User-Id") @Positive Long ownerId,
            @PathVariable Long requestId) {
        return itemRequestService.getById(ownerId, requestId);
    }
}