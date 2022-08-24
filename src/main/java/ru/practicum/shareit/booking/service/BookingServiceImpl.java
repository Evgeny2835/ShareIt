package ru.practicum.shareit.booking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final BookingMapper bookingMapper;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              ItemRepository itemRepository,
                              UserService userService,
                              BookingMapper bookingMapper) {
        this.bookingRepository = bookingRepository;
        this.itemRepository = itemRepository;
        this.userService = userService;
        this.bookingMapper = bookingMapper;
    }

    @Override
    public Booking create(Long userId, BookingCreateDto bookingCreateDto) {
        if (!isDateValid(bookingCreateDto)) {
            throw new ValidationException("Invalid date");
        }
        Booking booking = bookingMapper.toBooking(bookingCreateDto);
        User user = userService.getById(userId);
        Item item = itemRepository.findById(bookingCreateDto.getItemId()).orElseThrow(() ->
                new NotFoundException(String.format("Item not found: id=%d", bookingCreateDto.getItemId())));
        if (userId.equals(item.getOwner().getId())) {
            throw new NotFoundException("Item cannot be booked by the owner");
        }
        booking.setItem(item);
        booking.setBooker(user);
        booking.setStatus(BookingStatus.WAITING);
        if (booking.getItem().getAvailable()) {
            log.info("Item is booked: id={}", booking.getItem().getId());
            return bookingRepository.save(booking);
        }
        throw new ValidationException("Item is not available");
    }

    @Override
    public Booking getById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new NotFoundException(String.format("Booking not found: id=%d", bookingId)));
        if (booking.getBooker().getId().equals(userId)
                || booking.getItem().getOwner().getId().equals(userId)) {
            return booking;
        }
        throw new NotFoundException(String.format("Wrong user id=%d", userId));
    }

    @Override
    public Booking approve(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new NotFoundException(String.format("Booking not found: id=%d", bookingId)));
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException(String.format("Booking not available: id=%d", bookingId));
        }
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Only owner can change the booking status");
        }
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return bookingRepository.save(booking);
    }

    @Override
    public List<Booking> getAllByBooker(Long userId, BookingState state) {
        if (!userService.isUserExists(userId)) {
            throw new NotFoundException(String.format("User not found: id=%d", userId));
        }
        List<Booking> bookings = bookingRepository.getBookingsByBookerId(userId)
                .stream()
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .collect(Collectors.toList());
        return getBookingsByState(state, bookings);
    }

    @Override
    public List<Booking> getAllByItemsOwner(Long userId, BookingState state) {
        if (!userService.isUserExists(userId)) {
            throw new NotFoundException(String.format("User not found: id=%d", userId));
        }
        List<Item> items = itemRepository.findAll()
                .stream()
                .filter(s -> s.getOwner().equals(userService.getById(userId)))
                .collect(Collectors.toList());
        if (items.isEmpty()) {
            throw new NotFoundException(String.format("User id=%d has no items", userId));
        }
        Set<Long> itemsIdByOwner = items
                .stream()
                .map(Item::getId)
                .collect(Collectors.toSet());
        List<Booking> bookings = bookingRepository.findAll()
                .stream()
                .filter(s -> itemsIdByOwner.contains(s.getItem().getId()))
                .sorted(Comparator.comparing(Booking::getStart).reversed())
                .collect(Collectors.toList());
        return getBookingsByState(state, bookings);
    }

    public List<Booking> getAllByItem(Item item) {
        return bookingRepository.findBookingsByItem(item);
    }

    private boolean isDateValid(BookingCreateDto dto) {
        return dto.getStart().isAfter(LocalDateTime.now())
                && dto.getEnd().isAfter(LocalDateTime.now())
                && dto.getStart().isBefore(dto.getEnd());
    }

    private List<Booking> getBookingsByState(BookingState state, List<Booking> bookings) {
        switch (state) {
            case ALL:
                return bookings;
            case CURRENT:
                return bookings
                        .stream()
                        .filter(s -> s.getStart().isBefore(LocalDateTime.now()))
                        .filter(s -> s.getEnd().isAfter(LocalDateTime.now()))
                        .collect(Collectors.toList());
            case PAST:
                return bookings
                        .stream()
                        .filter(s -> s.getStart().isBefore(LocalDateTime.now()))
                        .filter(s -> s.getEnd().isBefore(LocalDateTime.now()))
                        .collect(Collectors.toList());
            case FUTURE:
                return bookings
                        .stream()
                        .filter(s -> s.getStart().isAfter(LocalDateTime.now()))
                        .filter(s -> s.getEnd().isAfter(LocalDateTime.now()))
                        .collect(Collectors.toList());
            case WAITING:
            case REJECTED:
                return bookings
                        .stream()
                        .filter(s -> s.getStatus().equals(BookingStatus.valueOf(state.toString())))
                        .collect(Collectors.toList());
            default:
                throw new ValidationException("Unknown state: " + state);
        }
    }
}