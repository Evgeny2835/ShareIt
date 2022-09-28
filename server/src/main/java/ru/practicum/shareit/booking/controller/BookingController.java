package ru.practicum.shareit.booking.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@Validated
public class BookingController {
    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingDto create(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                             @Valid @RequestBody BookingCreateDto bookingCreateDto) {
        return bookingService.create(userId, bookingCreateDto);
    }

    @GetMapping("/{bookingId}")
    public BookingDto get(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                          @PathVariable @Positive long bookingId) {
        return bookingService.getById(userId, bookingId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approve(
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId,
            @PathVariable @Positive Long bookingId,
            @RequestParam @NotNull Boolean approved) {
        return bookingService.approve(userId, bookingId, approved);
    }

    @GetMapping
    public List<BookingDto> getAllByBooker(
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId,
            @RequestParam(defaultValue = "ALL") BookingState state,
            @RequestParam(name = "from", required = false) @PositiveOrZero Integer from,
            @RequestParam(name = "size", required = false) @Positive Integer size) {
        return bookingService.getAllByBooker(userId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllByItemsOwner(
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId,
            @RequestParam(defaultValue = "ALL") BookingState state,
            @RequestParam(name = "from", required = false) @PositiveOrZero Integer from,
            @RequestParam(name = "size", required = false) @Positive Integer size) {
        return bookingService.getAllByItemsOwner(userId, state, from, size);
    }
}