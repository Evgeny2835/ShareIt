package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Optional;

public interface ItemRepository {
    Item add(Item item);

    Item update(Item item);

    void deleteItem(Long id);

    Optional<Item> getById(Long id);

    Collection<Item> getUserItems(Long userId);

    Collection<Item> keywordSearch(String keyword);
}