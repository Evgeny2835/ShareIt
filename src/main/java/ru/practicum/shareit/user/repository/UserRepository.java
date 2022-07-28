package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserRepository {
    User add(User user);

    User update(User user);

    void deleteUser(Long id);

    Optional<User> getById(Long id);

    Collection<User> getUsers();

    boolean isUserIdExists(Long id);

    boolean isUserEmailExists(String email);
}