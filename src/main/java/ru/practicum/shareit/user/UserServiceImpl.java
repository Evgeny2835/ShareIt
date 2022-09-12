package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.EmailDuplicateException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto create(UserDto userDto) {
        try {
            User user = UserMapper.toUser(userDto);
            log.info("New user added: email={}", user.getEmail());
            return UserMapper.toUserDto(userRepository.save(user));
        } catch (DataIntegrityViolationException e) {
            throw new EmailDuplicateException("Email exists");
        }
    }

    public UserDto update(UserDto userDto, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException(String.format("User not found: id=%d", userId)));
        try {
            if (userDto.getEmail() != null) {
                user.setEmail(userDto.getEmail());
            }
            if (userDto.getName() != null) {
                user.setName(userDto.getName());
            }
            log.info("User updated: id={}", user.getId());
            return UserMapper.toUserDto(userRepository.save(user));
        } catch (DataIntegrityViolationException e) {
            throw new EmailDuplicateException("Email exists");
        }
    }

    public void deleteUser(Long userId) {
        validateUserId(userId);
        log.info("User deleted: id={}", userId);
        userRepository.deleteById(userId);
    }

    public User getById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException(String.format("User not found: id=%d", userId)));
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    private void validateUserId(Long userId) {
        getById(userId);
    }
}