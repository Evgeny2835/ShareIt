package ru.practicum.shareit.requests.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.mapper.ItemRequestMapper;
import ru.practicum.shareit.requests.model.ItemRequest;
import ru.practicum.shareit.requests.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final UserService userService;
    private final ItemRequestMapper requestMapper;

    @Autowired
    public ItemRequestServiceImpl(ItemRequestRepository itemRequestRepository,
                                  UserService userService,
                                  ItemRequestMapper itemRequestMapper) {
        this.requestRepository = itemRequestRepository;
        this.userService = userService;
        this.requestMapper = itemRequestMapper;
    }

    @Override
    public ItemRequestDto create(Long ownerId, ItemRequestDto dto) {
        User user = userService.getById(ownerId);
        ItemRequest request = requestMapper.toItemRequest(dto, user);
        log.info("User id={} added new itemRequest", ownerId);
        return requestMapper.toItemRequestDto(requestRepository.save(request));
    }

    @Override
    public List<ItemRequestDto> getByUser(Long ownerId) {
        validateUserId(ownerId);
        log.info("List user requests has been compiled: userId={}", ownerId);
        return requestRepository.findAllByOwnerId(ownerId)
                .stream()
                .sorted(Comparator.comparing(ItemRequest::getCreated).reversed())
                .map(requestMapper::toItemRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAll(Long ownerId, Integer from, Integer size) {
        validateUserId(ownerId);
        PageRequest pageRequest = PageRequest.of(from / size, size, Sort.by("created").descending());
        log.info("List of requests from other users has been compiled, ownerId={}, from={}, size={}",
                ownerId, from, size);
        return requestRepository.findAllByOwnerIdIsNot(ownerId, pageRequest).getContent()
                .stream()
                .map(requestMapper::toItemRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getById(Long ownerId, Long requestId) {
        validateUserId(ownerId);
        ItemRequest request = requestRepository
                .findById(requestId)
                .orElseThrow(() -> new NotFoundException(String.format("Request not found: id=%d", requestId)));
        log.info("Request has been compiled: requestId={}", requestId);
        return requestMapper.toItemRequestDto(request);
    }

    private void validateUserId(Long userId) {
        userService.getById(userId);
    }
}