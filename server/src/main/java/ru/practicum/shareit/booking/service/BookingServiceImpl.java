package ru.practicum.shareit.booking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
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
    public BookingDto create(Long userId, BookingCreateDto bookingCreateDto) {
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
            return bookingMapper.toBookingDto(bookingRepository.save(booking));
        }
        throw new ValidationException("Item is not available");
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        Booking booking = validateIsBookingIdExistAndReturnBooking(bookingId);
        if (booking.getBooker().getId().equals(userId)
                || booking.getItem().getOwner().getId().equals(userId)) {
            return bookingMapper.toBookingDto(booking);
        }
        throw new NotFoundException(String.format("Wrong user id=%d", userId));
    }

    @Override
    public BookingDto approve(Long userId, Long bookingId, Boolean approved) {
        Booking booking = validateIsBookingIdExistAndReturnBooking(bookingId);
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException(String.format("Booking not available: id=%d", bookingId));
        }
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Only owner can change the booking status");
        }
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return bookingMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Override
    public List<BookingDto> getAllByBooker(Long userId, BookingState state, Integer from, Integer size) {
        validateUserId(userId);
        List<Booking> bookings;
        if (from == null || size == null) {
            bookings = bookingRepository.findAllByBooker_IdOrderByStartDesc(userId);
        } else {
            Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").descending());
            bookings = bookingRepository.getBookingsByBookerId(userId, pageable);
        }
        return getBookingsByState(state, bookings)
                .stream()
                .map(bookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getAllByItemsOwner(Long userId, BookingState state, Integer from, Integer size) {
        validateUserId(userId);
        List<Item> items = itemRepository.findAllByOwnerId(userId);
        if (items.isEmpty()) {
            throw new NotFoundException(String.format("User id=%d has no items", userId));
        }
        List<Booking> bookings;
        if (from == null || size == null) {
            Set<Long> itemsIdByOwner = items
                    .stream()
                    .map(Item::getId)
                    .collect(Collectors.toSet());
            bookings = bookingRepository.findAll()
                    .stream()
                    .filter(s -> itemsIdByOwner.contains(s.getItem().getId()))
                    .sorted(Comparator.comparing(Booking::getStart).reversed())
                    .collect(Collectors.toList());
        } else {
            Pageable pageable = PageRequest.of(from / size, size, Sort.by("start").descending());
            bookings = bookingRepository.findAllByItemIn(items, pageable).getContent();
        }
        return getBookingsByState(state, bookings)
                .stream()
                .map(bookingMapper::toBookingDto)
                .collect(Collectors.toList());
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

    private void validateUserId(Long userId) {
        userService.getById(userId);
    }

    private Booking validateIsBookingIdExistAndReturnBooking(Long bookingId) {
        return bookingRepository.findById(bookingId).orElseThrow(() ->
                new NotFoundException(String.format("Booking not found: id=%d", bookingId)));
    }
}