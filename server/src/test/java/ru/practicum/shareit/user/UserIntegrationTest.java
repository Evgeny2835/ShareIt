package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserIntegrationTest {
    private final JdbcTemplate jdbcTemplate;
    private final UserService userService;
    private final UserCreateDto userCreateDto = UserCreateDto.builder()
            .name("test")
            .email("test@yandex.ru")
            .build();

    @Test
    void create_shouldReturnNewUser() {
        userService.create(userCreateDto);

        String sql = "select * from users where name = ? and email = ?";

        User user = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToUser(rs),
                        userCreateDto.getName(), userCreateDto.getEmail())
                .stream()
                .findFirst()
                .orElse(null);

        assertNotNull(user);
        assertThat(user.getId(), isA(Long.class));
        assertThat(user.getName(), equalTo(userCreateDto.getName()));
        assertThat(user.getEmail(), equalTo(userCreateDto.getEmail()));
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("email"));
    }
}