package ru.practicum.shareit.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class InMemoryUserRepository implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private Long userId = 0L;

    @Override
    public User add(User user) {
        user.setId(generateId());
        users.put(user.getId(), user);
        log.info("New user added: id={}", user.getId());
        return user;
    }

    @Override
    public User update(User user) {
        users.put(user.getId(), user);
        log.info("User updated: id={}", user.getId());
        return user;
    }

    @Override
    public void deleteUser(Long userId) {
        users.remove(userId);
        log.info("User deleted: id={}", userId);
    }

    @Override
    public Optional<User> getById(Long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public Collection<User> getUsers() {
        return users.values();
    }

    @Override
    public boolean isUserIdExists(Long userId) {
        return !users.containsKey(userId);
    }

    @Override
    public boolean isUserEmailExists(String email) {
        return users.values().stream()
                .anyMatch(u -> u.getEmail().equals(email));
    }

    private Long generateId() {
        return ++userId;
    }
}