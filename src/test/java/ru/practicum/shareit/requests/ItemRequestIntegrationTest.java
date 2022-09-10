package ru.practicum.shareit.requests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.model.ItemRequest;
import ru.practicum.shareit.requests.service.ItemRequestService;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestIntegrationTest {
    final JdbcTemplate jdbcTemplate;
    final UserService userService;
    final ItemRequestService itemRequestService;
    private static final String REQUEST_DESCRIPTION = "request_description";
    final UserDto userCreateDto = UserDto.builder()
            .name("user_name")
            .email("user_email@yandex.ru")
            .build();
    final ItemRequestDto requestCreateDto = ItemRequestDto.builder()
            .description(REQUEST_DESCRIPTION)
            .build();

    @Test
    void create_shouldSaveInDataBaseAndReturnRequest() {
        UserDto userDto = userService.create(userCreateDto);
        ItemRequestDto itemRequestDto = itemRequestService.create(userDto.getId(), requestCreateDto);

        String sql = "select * from requests where owner_id = ?";

        ItemRequest result = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToItemRequest(rs),
                        itemRequestDto.getOwnerId())
                .stream()
                .findFirst()
                .orElse(null);

        assertNotNull(itemRequestDto);
        assertNotNull(result);
        assertThat(result.getId(), equalTo(itemRequestDto.getId()));
        assertThat(result.getDescription(), equalTo(itemRequestDto.getDescription()));
        assertThat(result.getCreated(), equalTo(itemRequestDto.getCreated()));
        assertThat(result.getOwner().getId(), equalTo(itemRequestDto.getOwnerId()));

    }

    private ItemRequest mapRowToItemRequest(ResultSet rs) throws SQLException {
        return ItemRequest.builder()
                .id(rs.getLong("id"))
                .description(rs.getString("description"))
                .created(rs.getTimestamp("created").toLocalDateTime())
                .owner(User.builder().id(rs.getLong("owner_id")).build())
                .build();
    }
}