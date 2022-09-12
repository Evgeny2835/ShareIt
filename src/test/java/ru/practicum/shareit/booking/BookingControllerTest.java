package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {
    private static final String URL = "/bookings";
    private static final Long USER_ID_WRONG = 5L;
    private static final Long USER_ID = 1L;
    private static final Long ITEM_ID = 1L;
    private static final Long BOOKING_ID = 1L;
    private static final Long BOOKING_ID_WRONG = 7L;
    private final LocalDateTime start = LocalDateTime.now().plusDays(1);
    private final LocalDateTime end = start.plusDays(1);
    private final User user = User.builder()
            .id(USER_ID)
            .name("user_name")
            .email("user_email@yandex.ru")
            .build();
    private final Item item = Item.builder()
            .id(ITEM_ID)
            .name("item_name")
            .description("item_description")
            .available(true)
            .owner(user)
            .build();
    private final BookingCreateDto bookingCreateDto = BookingCreateDto.builder()
            .start(start)
            .end(end)
            .itemId(ITEM_ID)
            .build();
    private final Booking booking = Booking.builder()
            .id(1L)
            .start(start)
            .end(end)
            .item(item)
            .booker(user)
            .status(BookingStatus.WAITING)
            .build();
    private final BookingDto bookingDto = BookingDto.builder()
            .id(1L)
            .start(start)
            .end(end)
            .item(item)
            .booker(user)
            .status(BookingStatus.WAITING)
            .build();
    @MockBean
    private BookingService bookingService;
    @MockBean
    private BookingMapper bookingMapper;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    void reset() {
        booking.setStatus(BookingStatus.WAITING);
        bookingDto.setStatus(BookingStatus.WAITING);
    }

    @Test
    void create_shouldReturnBookingWithStatus200() throws Exception {
        when(bookingService.create(USER_ID, bookingCreateDto))
                .thenReturn(booking);
        when(bookingMapper.toBookingDtoOutput(booking)).thenReturn(bookingDto);

        mockMvc.perform(post(URL)
                        .header("X-Sharer-User-Id", USER_ID)
                        .content(objectMapper.writeValueAsString(bookingCreateDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().toString())));
    }

    @Test
    void create_shouldAnswer404WhenUserIsOwnerOfItem() throws Exception {
        when(bookingService.create(USER_ID, bookingCreateDto))
                .thenThrow(NotFoundException.class);

        mockMvc.perform(post(URL)
                        .header("X-Sharer-User-Id", USER_ID)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().is(404));
    }

    @Test
    void getById_shouldReturnBookingAndAnswer200() throws Exception {
        when(bookingService.getById(USER_ID, BOOKING_ID)).thenReturn(booking);
        when(bookingMapper.toBookingDtoOutput(booking)).thenReturn(bookingDto);

        mockMvc.perform(get(URL + "/{bookingId}", BOOKING_ID)
                        .header("X-Sharer-User-Id", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().toString())));
    }

    @Test
    void getById_shouldAnswer404WhenUserIsNotBookerOrUserIsNotItemOwner() throws Exception {
        when(bookingService.getById(USER_ID_WRONG, BOOKING_ID)).thenThrow(NotFoundException.class);
        when(bookingService.getById(USER_ID, BOOKING_ID_WRONG)).thenThrow(NotFoundException.class);

        mockMvc.perform(get(URL + "/{bookingId}", BOOKING_ID)
                        .header("X-Sharer-User-Id", USER_ID_WRONG))
                .andExpect(status().is(404));

        mockMvc.perform(get(URL + "/{bookingId}", BOOKING_ID_WRONG)
                        .header("X-Sharer-User-Id", USER_ID))
                .andExpect(status().is(404));
    }

    @Test
    void approve_shouldAnswer200AndReturnStatusAPPROVEDWhenApprovedIsTrue() throws Exception {
        booking.setStatus(BookingStatus.APPROVED);
        bookingDto.setStatus(BookingStatus.APPROVED);

        when(bookingService.approve(USER_ID, BOOKING_ID, true)).thenReturn(booking);
        when(bookingMapper.toBookingDtoOutput(booking)).thenReturn(bookingDto);

        mockMvc.perform(patch(URL + "/{bookingId}", BOOKING_ID)
                        .header("X-Sharer-User-Id", USER_ID)
                        .param("approved", String.valueOf(true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("APPROVED")));
    }

    @Test
    void approve_shouldAnswer200AndReturnStatusREJECTEDWhenApprovedIsFalse() throws Exception {
        booking.setStatus(BookingStatus.REJECTED);
        bookingDto.setStatus(BookingStatus.REJECTED);

        when(bookingService.approve(USER_ID, BOOKING_ID, false)).thenReturn(booking);
        when(bookingMapper.toBookingDtoOutput(booking)).thenReturn(bookingDto);

        mockMvc.perform(patch(URL + "/{bookingId}", BOOKING_ID)
                        .header("X-Sharer-User-Id", USER_ID)
                        .param("approved", String.valueOf(false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("REJECTED")));
    }

    @Test
    void approve_shouldAnswer404WhenBookingNotFondOrUserIsNotOwner() throws Exception {
        when(bookingService.approve(USER_ID_WRONG, BOOKING_ID, true)).thenThrow(NotFoundException.class);
        when(bookingService.approve(USER_ID, BOOKING_ID_WRONG, true)).thenThrow(NotFoundException.class);

        mockMvc.perform(patch(URL + "/{bookingId}", BOOKING_ID_WRONG)
                        .header("X-Sharer-User-Id", USER_ID)
                        .param("approved", String.valueOf(true)))
                .andExpect(status().is(404));

        mockMvc.perform(patch(URL + "/{bookingId}", BOOKING_ID)
                        .header("X-Sharer-User-Id", USER_ID_WRONG)
                        .param("approved", String.valueOf(true)))
                .andExpect(status().is(404));
    }

    @Test
    void getAllByBooker_shouldAnswer200AndReturnListOfOneBooking() throws Exception {
        when(bookingService.getAllByBooker(any(), any(), any(), any())).thenReturn(List.of(bookingDto));

        mockMvc.perform(get(URL)
                        .header("X-Sharer-User-Id", USER_ID)
                        .param("state", String.valueOf(BookingState.ALL)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)));
    }

    @Test
    void getAllByItemsOwner_shouldAnswer200AndReturnListOfOneBooking() throws Exception {
        when(bookingService.getAllByItemsOwner(any(), any(), any(), any())).thenReturn(List.of(bookingDto));

        mockMvc.perform(get(URL + "/owner")
                        .header("X-Sharer-User-Id", USER_ID)
                        .param("state", String.valueOf(BookingState.ALL)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)));

    }

    @Test
    void getAllByItemsOwner_shouldAnswer400WhenInvalidState() throws Exception {
        when(bookingService.getAllByItemsOwner(any(), any(), any(), any())).thenThrow(ValidationException.class);

        mockMvc.perform(get(URL + "/owner")
                        .header("X-Sharer-User-Id", USER_ID)
                        .param("state", "INVALID"))
                .andExpect(status().is(400));
    }
}
