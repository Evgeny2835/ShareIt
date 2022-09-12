package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.requests.model.ItemRequest;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemIntegrationTest {
    private final JdbcTemplate jdbcTemplate;
    private final ItemService itemService;
    private final UserService userService;
    private final UserDto newUserDto = UserDto.builder()
            .name("user_name")
            .email("user_email@yandex.ru")
            .build();

    @Test
    void create_shouldReturnNewUser() {
        UserDto userDto = userService.create(newUserDto);
        ItemDto newItemDto = ItemDto.builder()
                .name("item_name")
                .description("item_description")
                .available(true)
                .ownerId(userDto.getId())
                .build();
        itemService.create(userDto.getId(), newItemDto);

        String sql = "select * from items where name = ?";

        Item item = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToItem(rs),
                        newItemDto.getName())
                .stream()
                .findFirst()
                .orElse(null);

        assertNotNull(item);
        assertThat(item.getId(), notNullValue());
        assertThat(item.getName(), equalTo(newItemDto.getName()));
        assertThat(item.getDescription(), equalTo(newItemDto.getDescription()));
        assertThat(item.getAvailable(), equalTo(newItemDto.getAvailable()));
        assertThat(item.getOwner().getId(), equalTo(newItemDto.getOwnerId()));
    }

    private Item mapRowToItem(ResultSet rs) throws SQLException {
        return new Item(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getObject("is_available", Boolean.class),
                User.builder().id(rs.getLong("owner_id")).build(),
                ItemRequest.builder().id(rs.getLong("request_id")).build());
    }
}