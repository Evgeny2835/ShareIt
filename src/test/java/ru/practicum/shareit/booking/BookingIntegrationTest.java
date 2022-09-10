package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingIntegrationTest {
    final JdbcTemplate jdbcTemplate;
    final ItemService itemService;
    final UserService userService;
    final BookingService bookingService;
    UserDto newUserDtoOwner = UserDto.builder()
            .name("user_name")
            .email("user_email@yandex.ru")
            .build();
    UserDto newUserDtoBooker = UserDto.builder()
            .name("booker_name")
            .email("booker_email@yandex.ru")
            .build();

    @Test
    void create_shouldSaveInDataBaseAndReturnNewBooking() {
        UserDto userDtoOwner = userService.create(newUserDtoOwner);
        UserDto userDtoBooker = userService.create(newUserDtoBooker);
        ItemDto newItemDto = ItemDto.builder()
                .name("item_name")
                .description("item_description")
                .available(true)
                .ownerId(userDtoOwner.getId())
                .build();

        ItemDto itemDto = itemService.create(userDtoOwner.getId(), newItemDto);

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        BookingCreateDto bookingCreateDto = BookingCreateDto.builder()
                .start(start)
                .end(end)
                .itemId(itemDto.getId())
                .build();

        Booking booking = bookingService.create(userDtoBooker.getId(), bookingCreateDto);

        String sql = "select * from bookings where item_id = ?";

        Booking result = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToBooking(rs),
                        bookingCreateDto.getItemId())
                .stream()
                .findFirst()
                .orElse(null);

        assertNotNull(booking);
        assertNotNull(result);
        assertThat(result.getId(), equalTo(booking.getId()));
        assertThat(result.getStart(), equalTo(booking.getStart()));
        assertThat(result.getEnd(), equalTo(booking.getEnd()));
        assertThat(result.getItem().getId(), equalTo(booking.getItem().getId()));
        assertThat(result.getBooker().getId(), equalTo(booking.getBooker().getId()));
        assertThat(result.getStatus(), equalTo(booking.getStatus()));
    }

    private Booking mapRowToBooking(ResultSet rs) throws SQLException {
        return new Booking(
                rs.getLong("id"),
                rs.getTimestamp("start_time").toLocalDateTime(),
                rs.getTimestamp("end_time").toLocalDateTime(),
                Item.builder().id(rs.getLong("item_id")).build(),
                User.builder().id(rs.getLong("booker_id")).build(),
                BookingStatus.valueOf(rs.getString("status")));
    }
}