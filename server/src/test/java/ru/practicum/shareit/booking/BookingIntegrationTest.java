package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.dto.UserCreateDto;
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
    private final JdbcTemplate jdbcTemplate;
    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;
    private final UserCreateDto userCreateDtoOwner = UserCreateDto.builder()
            .name("user_name")
            .email("user_email@yandex.ru")
            .build();
    private final UserCreateDto userCreateDtoBooker = UserCreateDto.builder()
            .name("booker_name")
            .email("booker_email@yandex.ru")
            .build();

    @Test
    void create_shouldSaveInDataBaseAndReturnNewBooking() {
        UserDto userDtoOwner = userService.create(userCreateDtoOwner);
        UserDto userDtoBooker = userService.create(userCreateDtoBooker);
        ItemCreateDto itemCreateDto = ItemCreateDto.builder()
                .name("item_name")
                .description("item_description")
                .available(true)
                .build();

        ItemDto itemDto = itemService.create(userDtoOwner.getId(), itemCreateDto);

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(1);

        BookingCreateDto bookingCreateDto = BookingCreateDto.builder()
                .start(start)
                .end(end)
                .itemId(itemDto.getId())
                .build();

        BookingDto booking = bookingService.create(userDtoBooker.getId(), bookingCreateDto);

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