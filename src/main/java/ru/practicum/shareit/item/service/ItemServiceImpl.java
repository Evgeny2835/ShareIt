package ru.practicum.shareit.item.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, UserService userService) {
        this.itemRepository = itemRepository;
        this.userService = userService;
    }

    @Override
    public ItemDto add(Long userId, ItemDto itemDto) {
        if (userService.isUserIdExists(userId)) {
            throw new NotFoundException(String.format("User not found: id=%d", userId));
        }
        Item item = ItemMapper.toItem(itemDto, userId);
        return ItemMapper.toItemDto(itemRepository.add(item));
    }

    @Override
    public ItemDto update(Long userId, ItemDto itemDto, Long itemId) {
        if (userService.isUserIdExists(userId)) {
            throw new NotFoundException(String.format("User not found: id=%d", userId));
        }
        Item item = itemRepository.getById(itemId).orElseThrow(() ->
                new NotFoundException(String.format("Item not found: id=%d", itemId)));
        if (!item.getOwner().equals(userId)) {
            throw new NotFoundException(String.format("User id=%d is not the owner item id=%d", userId, itemId));
        }
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        return ItemMapper.toItemDto(itemRepository.update(item));
    }

    @Override
    public void deleteItem(Long userId, Long itemId) {
        if (userService.isUserIdExists(userId)) {
            throw new NotFoundException(String.format("User not found: id=%d", userId));
        }
        Item item = itemRepository.getById(itemId).orElseThrow(() ->
                new NotFoundException(String.format("Item not found: id=%d", itemId)));
        if (!item.getOwner().equals(userId)) {
            throw new NotFoundException(String.format("User id=%d is not the owner item id=%d", userId, itemId));
        }
        itemRepository.deleteItem(itemId);
    }

    @Override
    public ItemDto getById(Long id) {
        Item item = itemRepository.getById(id).orElseThrow(() ->
                new NotFoundException(String.format("Item not found: id=%d", id)));
        return ItemMapper.toItemDto(item);
    }

    public Collection<ItemDto> getUserItems(Long userId) {
        if (userService.isUserIdExists(userId)) {
            throw new NotFoundException(String.format("User not found: id=%d", userId));
        }
        return itemRepository.getUserItems(userId)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    public Collection<ItemDto> keywordSearch(String keyword) {
        return itemRepository.keywordSearch(keyword)
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}