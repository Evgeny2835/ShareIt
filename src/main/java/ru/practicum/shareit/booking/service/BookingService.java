package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface BookingService {

    Booking create(Long userId, BookingCreateDto bookingCreateDto);

    Booking getById(Long userId, Long bookingId);

    Booking approve(Long userId, Long bookingId, Boolean approved);

    List<Booking> getAllByBooker(Long userId, BookingState state);

    List<Booking> getAllByItemsOwner(Long userId, BookingState state);

    //Collection<Booking> getAllByItem(Long itemId);

    List<Booking> getAllByItem(Item item);
}