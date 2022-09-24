package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.client.BookingClient;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;
    private static final String API_PREFIX_OWNER = "/owner";

    @PostMapping
    public ResponseEntity<Object> createBooking(
            @RequestHeader("X-Sharer-User-Id") @Positive long userId,
            @RequestBody @Valid BookingCreateDto bookingCreateDto) {
        log.info("Creating booking {}, userId={}", bookingCreateDto, userId);
        return bookingClient.createBooking(userId, bookingCreateDto);
    }

    @GetMapping("{bookingId}")
    public ResponseEntity<Object> getBooking(
            @RequestHeader("X-Sharer-User-Id") @Positive long userId,
            @PathVariable @Positive long bookingId) {
        log.info("Get booking, userId={}, bookingId={}", userId, bookingId);
        return bookingClient.getBooking(userId, bookingId);
    }

    @PatchMapping("{bookingId}")
    public ResponseEntity<Object> approveBooking(
            @RequestHeader("X-Sharer-User-Id") @Positive long userId,
            @PathVariable @Positive long bookingId,
            @RequestParam @NotNull Boolean approved) {
        log.info("Approve booking, userId={}, bookingId={}", userId, bookingId);
        return bookingClient.approveBooking(userId, bookingId, approved);
    }

    @GetMapping
    public ResponseEntity<Object> getBookingsByBooker(
            @RequestHeader("X-Sharer-User-Id") @Positive long userId,
            @RequestParam(name = "state", defaultValue = "ALL") String bookingState,
            @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(name = "size", defaultValue = "10") @Positive Integer size) {
        BookingState state = validateParamAndReturnBookingState(bookingState);
        log.info("Get bookings by bookerId={} with state={}, from={}, size={}", userId, state, from, size);
        return bookingClient.getBookingsByBooker(userId, state, from, size);
    }

    @GetMapping(API_PREFIX_OWNER)
    public ResponseEntity<Object> getAllByItemsOwner(
            @RequestHeader("X-Sharer-User-Id") @Positive long userId,
            @RequestParam(name = "state", defaultValue = "ALL") String bookingState,
            @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(name = "size", defaultValue = "10") @Positive Integer size) {
        BookingState state = validateParamAndReturnBookingState(bookingState);
        log.info("Get bookings by itemsOwnerId={} with state={}, from={}, size={}", userId, state, from, size);
        return bookingClient.getAllByItemsOwner(API_PREFIX_OWNER, userId, state, from, size);
    }

    private BookingState validateParamAndReturnBookingState(String bookingState) {
        return BookingState.from(bookingState).orElseThrow(() ->
                new IllegalArgumentException("Unknown state: " + bookingState));
    }
}