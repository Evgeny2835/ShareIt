package ru.practicum.shareit.item.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InMemoryItemRepository implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private Long itemId = 0L;

    @Override
    public Item add(Item item) {
        item.setId(generateId());
        items.put(item.getId(), item);
        log.info("New item added: id={}", item.getId());
        return item;
    }

    @Override
    public Item update(Item item) {
        items.put(item.getId(), item);
        log.info("Item updated: id={}", item.getId());
        return item;
    }

    @Override
    public void deleteItem(Long itemId) {
        items.remove(itemId);
        log.info("Item deleted: id={}", itemId);
    }

    @Override
    public Optional<Item> getById(Long itemId) {
        return Optional.ofNullable(items.get(itemId));
    }

    public Collection<Item> getUserItems(Long userId) {
        return items.values()
                .stream()
                .filter(s -> s.getOwner().equals(userId))
                .collect(Collectors.toList());
    }

    public Collection<Item> keywordSearch(String keyword) {
        return items.values()
                .stream()
                .filter((Item::getAvailable))
                .filter(s -> s.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                        s.getDescription().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    private Long generateId() {
        return ++itemId;
    }
}