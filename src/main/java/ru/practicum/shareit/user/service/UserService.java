package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserService {
    UserDto add(UserDto userDto);

    UserDto update(UserDto userDto, Long userId);

    void deleteUser(Long userId);

    User getById(Long userId);

    Collection<User> getUsers();

    boolean isUserIdExists(Long userId);
}