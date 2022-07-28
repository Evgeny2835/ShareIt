package ru.practicum.shareit.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.EmailDuplicationException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto add(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        if (userRepository.isUserEmailExists(user.getEmail())) {
            throw new EmailDuplicationException(String.format("User with email exists: %s", user.getEmail()));
        }
        return UserMapper.toUserDto(userRepository.add(user));
    }

    public UserDto update(UserDto userDto, Long userId) {
        if (userDto.getEmail() != null &&
                userRepository.isUserEmailExists(userDto.getEmail())) {
            throw new EmailDuplicationException(String.format("User with email exists: %s", userDto.getEmail()));
        }
        User user = userRepository.getById(userId).orElseThrow(() ->
                new NotFoundException(String.format("User not found: id=%d", userId)));
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        return UserMapper.toUserDto(userRepository.update(user));
    }

    public void deleteUser(Long userId) {
        if (userRepository.isUserIdExists(userId)) {
            throw new NotFoundException(String.format("User not found: id=%d", userId));
        }
        userRepository.deleteUser(userId);
    }

    public User getById(Long userId) {
        return userRepository.getById(userId).orElseThrow(() ->
                new NotFoundException(String.format("User not found: id=%d", userId)));
    }

    public Collection<User> getUsers() {
        return userRepository.getUsers();
    }

    public boolean isUserIdExists(Long userId) {
        return userRepository.isUserIdExists(userId);
    }
}