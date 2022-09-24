package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    UserDto create(UserCreateDto userCreateDto);

    UserDto update(UserDto userDto, Long userId);

    void deleteUser(Long userId);

    User getById(Long userId);

    List<User> getUsers();
}