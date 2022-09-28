package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface BookingService {

    BookingDto create(Long userId, BookingCreateDto bookingCreateDto);

    BookingDto getById(Long userId, Long bookingId);

    BookingDto approve(Long userId, Long bookingId, Boolean approved);

    List<BookingDto> getAllByBooker(Long userId, BookingState state, Integer from, Integer size);

    List<BookingDto> getAllByItemsOwner(Long userId, BookingState state, Integer from, Integer size);

    List<Booking> getAllByItem(Item item);
}