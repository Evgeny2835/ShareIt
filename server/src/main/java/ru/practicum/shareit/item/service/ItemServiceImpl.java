package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.comment.dto.CommentCreateDto;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.mapper.CommentMapper;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.comment.repository.CommentRepository;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final ItemMapper itemMapper;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final CommentMapper commentMapper;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository,
                           UserService userService,
                           ItemMapper itemMapper,
                           CommentRepository commentRepository,
                           BookingRepository bookingRepository,
                           CommentMapper commentMapper) {
        this.itemRepository = itemRepository;
        this.userService = userService;
        this.itemMapper = itemMapper;
        this.commentRepository = commentRepository;
        this.bookingRepository = bookingRepository;
        this.commentMapper = commentMapper;
    }

    @Override
    public ItemDto create(Long userId, ItemCreateDto itemCreateDto) {
        User user = userService.getById(userId);
        Item item = itemMapper.toItem(itemCreateDto, user);
        log.info("User id={} added new item '{}'", userId, item.getName());
        return itemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto update(Long userId, ItemDto itemDto, Long itemId) {
        validateUserId(userId);
        Item item = getById(itemId);
        checkUserIsItemOwner(userId, item);
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        log.info("Item updated: id={}", item.getId());
        return itemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public void deleteItem(Long itemId, Long userId) {
        validateUserId(userId);
        checkUserIsItemOwner(userId, getById(itemId));
        log.info("Item deleted: id={}", itemId);
        itemRepository.deleteById(itemId);
    }

    @Override
    public Item getById(Long itemId) {
        return itemRepository.findById(itemId).orElseThrow(() ->
                new NotFoundException(String.format("Item not found: id=%d", itemId)));
    }

    @Override
    public ItemDto getById(Long itemId, Long userId) {
        Item item = getById(itemId);
        validateUserId(userId);
        ItemDto itemDto = itemMapper.toItemDto(item);
        Set<Comment> comments = commentRepository.findCommentsByItem_Id(itemId);
        itemDto.setComments(comments
                .stream()
                .map(commentMapper::toCommentDto)
                .collect(Collectors.toSet()));
        if (Objects.equals(item.getOwner().getId(), userId)) {
            addLastAndNextBookings(itemDto);
        }
        return itemDto;
    }

    @Override
    public List<ItemDto> getUserItems(Long userId, Integer from, Integer size) {
        validateUserId(userId);
        PageRequest pageRequest = PageRequest.of(from / size, size, Sort.by("id"));
        List<Item> items = itemRepository.findAllByOwnerId(userId, pageRequest);
        log.info("List of items has been compiled");
        List<ItemDto> itemsDto = items.stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
        for (ItemDto i : itemsDto) {
            addLastAndNextBookings(i);
        }
        return itemsDto;
    }

    @Override
    public List<ItemDto> keywordSearch(String keyword, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Item> items = itemRepository.search(keyword, pageable);
        return items
                .stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto createComment(Long itemId, Long userId, CommentCreateDto commentCreateDto) {
        User user = userService.getById(userId);
        Item item = getById(itemId);
        Comment comment = new Comment(commentCreateDto.getText(), item, user, LocalDateTime.now());
        try {
            bookingRepository.getBookingsByBookerId(comment.getAuthor().getId())
                    .stream()
                    .filter(s -> s.getStart().isBefore(LocalDateTime.now()))
                    .filter(s -> s.getEnd().isBefore(LocalDateTime.now()))
                    .sorted(Comparator.comparing(Booking::getStart).reversed())
                    .filter(booking -> Objects.equals(booking.getItem().getId(), comment.getItem().getId()))
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("Not found booking for this user"));
        } catch (NotFoundException e) {
            throw new ValidationException("Comment can be created after using");
        }
        return commentMapper.toCommentDto(commentRepository.save(comment));
    }

    private void checkUserIsItemOwner(Long userId, Item item) {
        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException(String.format("User id=%d is not the owner item id=%d",
                    userId, item.getId()));
        }
        log.info("Checks on the existence of the user id={} and the item id={}, " +
                        "whether the user is the owner of the item, passed successfully",
                userId, item.getId());
    }

    private void validateUserId(Long userId) {
        userService.getById(userId);
    }

    private void addLastAndNextBookings(ItemDto itemDto) {
        List<Booking> bookings = bookingRepository.findBookingsByItem_Id(itemDto.getId());
        LocalDateTime now = LocalDateTime.now();
        itemDto.setLastBooking(bookings.stream()
                .filter(s -> s.getEnd().isBefore(now))
                .map(itemMapper::toBookingItemDto)
                .max(Comparator.comparing(BookingItemDto::getEnd))
                .orElse(null)
        );
        itemDto.setNextBooking(bookings.stream()
                .filter(s -> s.getStart().isAfter(now))
                .map(itemMapper::toBookingItemDto)
                .min(Comparator.comparing(BookingItemDto::getStart))
                .orElse(null)
        );
    }
}