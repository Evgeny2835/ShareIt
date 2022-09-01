package ru.practicum.shareit.item.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.comment.repository.CommentRepository;
import ru.practicum.shareit.item.comment.service.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.requests.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class ItemMapper {
    private final BookingService bookingService;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final ItemRequestRepository itemRequestRepository;

    public ItemMapper(BookingService bookingService,
                      CommentRepository commentRepository,
                      CommentMapper commentMapper,
                      ItemRequestRepository itemRequestRepository) {
        this.bookingService = bookingService;
        this.commentRepository = commentRepository;
        this.commentMapper = commentMapper;
        this.itemRequestRepository = itemRequestRepository;
    }

    public Item toItem(ItemDto itemDto, User owner) {
        return new Item(
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                owner,
                itemDto.getRequestId() != null ? itemRequestRepository
                        .findById(itemDto.getRequestId()).orElse(null) : null
        );
    }

    public ItemDto toItemDto(Item item, Long userId) {
        ItemDto itemDto = createItemDto(item);
        if (Objects.equals(userId, item.getOwner().getId())) {
            itemDto.setLastBooking(getLastBooking(bookingService.getAllByItem(item)));
            itemDto.setNextBooking(getNextBooking(bookingService.getAllByItem(item)));
        }
        return itemDto;
    }

    public ItemDto toItemDto(Item item) {
        ItemDto itemDto = createItemDto(item);
        itemDto.setLastBooking(getLastBooking(bookingService.getAllByItem(item)));
        itemDto.setNextBooking(getNextBooking(bookingService.getAllByItem(item)));
        return itemDto;
    }

    private BookingItemDto getLastBooking(List<Booking> bookings) {
        return bookings.stream()
                .map(this::toBookingItemDto)
                .sorted(Comparator.comparing(BookingItemDto::getEnd).reversed())
                .filter(booking -> LocalDateTime.now().isAfter(booking.getEnd()))
                .findFirst()
                .orElse(null);
    }

    private BookingItemDto getNextBooking(List<Booking> bookings) {
        return bookings.stream()
                .map(this::toBookingItemDto)
                .sorted(Comparator.comparing(BookingItemDto::getStart))
                .filter(booking -> LocalDateTime.now().isBefore(booking.getStart()))
                .findFirst()
                .orElse(null);
    }

    private BookingItemDto toBookingItemDto(Booking booking) {
        return BookingItemDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .bookerId(booking.getBooker().getId())
                .build();
    }

    private ItemDto createItemDto(Item item) {
        ItemDto itemDto = new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getOwner().getId(),
                item.getRequest() != null ? item.getRequest().getId() : null
        );
        itemDto.setComments(commentRepository.findCommentsByItem_Id(item.getId())
                .stream()
                .map(commentMapper::toCommentDto)
                .collect(Collectors.toSet()));
        return itemDto;
    }
}