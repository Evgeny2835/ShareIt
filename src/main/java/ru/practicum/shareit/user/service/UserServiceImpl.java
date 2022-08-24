package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto create(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        log.info("New user added: id={}", user.getId());
        return UserMapper.toUserDto(userRepository.save(user));
    }

    public UserDto update(UserDto userDto, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException(String.format("User not found: id=%d", userId)));
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        log.info("User updated: id={}", user.getId());
        return UserMapper.toUserDto(userRepository.save(user));
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("User not found: id=%d", userId));
        }
        log.info("User deleted: id={}", userId);
        userRepository.deleteById(userId);
    }

    public User getById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException(String.format("User not found: id=%d", userId)));
    }

    public Collection<User> getUsers() {
        return userRepository.findAll();
    }

    public boolean isUserExists(Long userId) {
        return userRepository.existsById(userId);
    }
}