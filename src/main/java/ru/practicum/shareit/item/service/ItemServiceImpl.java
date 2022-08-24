package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.comment.repository.CommentRepository;
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
import java.util.stream.Collectors;

@Service
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final ItemMapper itemMapper;
    private final CommentRepository commentRepository;
    private final BookingService bookingService;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository,
                           UserService userService,
                           ItemMapper itemMapper,
                           CommentRepository commentRepository,
                           BookingService bookingService) {
        this.itemRepository = itemRepository;
        this.userService = userService;
        this.itemMapper = itemMapper;
        this.commentRepository = commentRepository;
        this.bookingService = bookingService;
    }

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        User user = userService.getById(userId);
        Item item = itemMapper.toItem(itemDto, user);
        log.info("New item added: id={}", item.getId());
        return itemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto update(Long userId, ItemDto itemDto, Long itemId) {
        Item item = validateUserAndItemAndReturnItem(userId, itemId);
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
    public void deleteItem(Long userId, Long itemId) {
        validateUserAndItemAndReturnItem(userId, itemId);
        log.info("Item deleted: id={}", itemId);
        itemRepository.deleteById(itemId);
    }

    @Override
    public Item getById(Long itemId) {
        return itemRepository.findById(itemId).orElseThrow(() ->
                new NotFoundException(String.format("Item not found: id=%d", itemId)));
    }

    public List<Item> getUserItems(Long userId) {
        if (!userService.isUserExists(userId)) {
            throw new NotFoundException(String.format("User not found: id=%d", userId));
        }
        return itemRepository.findAll()
                .stream()
                .filter(s -> s.getOwner().equals(userService.getById(userId)))
                .sorted(Comparator.comparing(Item::getId))
                .collect(Collectors.toList());
    }

    public List<ItemDto> keywordSearch(String keyword) {
        return itemRepository.search(keyword)
                .stream()
                .filter((Item::getAvailable))
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public Comment createComment(Long userId, Long itemId, CommentDto commentDto) {
        User user = userService.getById(userId);
        Item item = itemRepository.findById(itemId).orElseThrow(() ->
                new NotFoundException(String.format("Item not found: id=%d", itemId)));
        Comment comment = new Comment(commentDto.getText(), item, user, LocalDateTime.now());
        try {
            bookingService.getAllByBooker(comment.getAuthor().getId(), BookingState.PAST).stream()
                    .filter(booking -> Objects.equals(booking.getItem().getId(), comment.getItem().getId()))
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("Not found booking for this user"));
        } catch (NotFoundException e) {
            throw new ValidationException("Comment can be created after using");
        }
        return commentRepository.save(comment);
    }

    private Item validateUserAndItemAndReturnItem(Long userId, Long itemId) {
        if (!userService.isUserExists(userId)) {
            throw new NotFoundException(String.format("User not found: id=%d", userId));
        }
        Item item = itemRepository.findById(itemId).orElseThrow(() ->
                new NotFoundException(String.format("Item not found: id=%d", itemId)));
        if (!item.getOwner().equals(userService.getById(userId))) {
            throw new NotFoundException(String.format("User id=%d is not the owner item id=%d", userId, itemId));
        }
        log.info("Checks on the existence of the user id={} and the item id={}, " +
                "whether the user is the owner of the item, passed successfully", userId, itemId);
        return item;
    }
}