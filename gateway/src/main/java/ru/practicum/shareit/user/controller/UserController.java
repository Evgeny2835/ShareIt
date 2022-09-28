package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.client.UserClient;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {
    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestBody @Valid UserCreateDto userCreateDto) {
        log.info("Creating user {}", userCreateDto);
        return userClient.create(userCreateDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable("id") @Positive long userId,
                                             @RequestBody @Valid UserDto userDto) {
        log.info("Updating user with id={}", userId);
        return userClient.update(userId, userDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable("id") @Positive long userId) {
        log.info("Deleting user with id={}", userId);
        return userClient.delete(userId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> get(@PathVariable("id") @Positive long userId) {
        log.info("Getting user with id={}", userId);
        return userClient.get(userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAll() {
        log.info("Getting all users");
        return userClient.getAll();
    }
}