package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    @Mock
    BookingRepository bookingRepository;
    @Mock
    UserService userService;
    @Mock
    BookingMapper bookingMapper;
    @Mock
    ItemRepository itemRepository;
    @InjectMocks
    BookingServiceImpl bookingService;
    private static final Long USER_ID_WRONG = 5L;
    private static final Long USER_ID = 1L;
    private static final Long ITEM_ID = 1L;
    private static final Long BOOKING_ID = 1L;
    final LocalDateTime start = LocalDateTime.now().plusDays(1);
    final LocalDateTime end = LocalDateTime.now().plusDays(2);
    final User user = User.builder()
            .id(USER_ID)
            .name("user_name")
            .email("user_email@yandex.ru")
            .build();
    final Item item = Item.builder()
            .id(ITEM_ID)
            .name("item_name")
            .description("item_description")
            .available(true)
            .owner(user)
            .build();
    final BookingCreateDto bookingCreateDto = BookingCreateDto.builder()
            .start(start)
            .end(end)
            .itemId(ITEM_ID)
            .build();
    final Booking booking = Booking.builder()
            .id(1L)
            .start(start)
            .end(end)
            .item(item)
            .booker(user)
            .status(BookingStatus.WAITING)
            .build();

    final BookingDto bookingDto = BookingDto.builder()
            .id(1L)
            .start(start)
            .end(end)
            .item(item)
            .booker(user)
            .status(BookingStatus.WAITING)
            .build();

    @Test
    void create_shouldCreateAndSaveBookingWhenUserIsNotOwnerAndItemIsAvailable() {

        when(bookingMapper.toBooking(bookingCreateDto)).thenReturn(booking);
        when(userService.getById(anyLong())).thenReturn(user);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(bookingRepository.save(any())).thenReturn(booking);

        Booking result = bookingService.create(USER_ID_WRONG, bookingCreateDto);

        verify(bookingRepository, times(1)).save(booking);
        assertNotNull(result);
        assertThat(result.getItem(), equalTo(item));
        assertThat(result.getBooker(), equalTo(user));
        assertThat(result.getStatus(), equalTo(booking.getStatus()));
    }

    @Test
    void create_shouldThrowNotFoundExceptionWhenUserIsOwnerOfItem() {

        assertThrows(NotFoundException.class, () -> bookingService.create(USER_ID, bookingCreateDto));

        verifyNoInteractions(bookingRepository);
    }

    @Test
    void getById_shouldReturnBooking() {

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        Booking result = bookingService.getById(USER_ID, BOOKING_ID);

        assertNotNull(result);
        assertThat(result.getId(), equalTo(booking.getId()));
        assertThat(result.getStart(), equalTo(booking.getStart()));
        assertThat(result.getEnd(), equalTo(booking.getEnd()));
        assertThat(result.getItem(), equalTo(booking.getItem()));
        assertThat(result.getBooker(), equalTo(booking.getBooker()));
        assertThat(result.getStatus(), equalTo(booking.getStatus()));
    }

    @Test
    void getById_shouldThrowNotFoundExceptionWhenRepositoryReturnOptionalEmpty() {

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.getById(USER_ID, BOOKING_ID));
    }

    @Test
    void getById_shouldThrowNotFoundExceptionWhenUserIsNotBookerOrUserIsNotItemOwner() {

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class, () -> bookingService.getById(USER_ID_WRONG, BOOKING_ID));
    }

    @Test
    void approve_shouldSetStatusAPPROVEDWhenApprovedIsTrue() {

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenAnswer(returnsFirstArg());

        Booking result = bookingService.approve(USER_ID, BOOKING_ID, true);

        assertNotNull(result);
        assertThat(result.getStatus(), equalTo(BookingStatus.APPROVED));
    }

    @Test
    void approve_shouldSetStatusREJECTEDWhenApprovedIsFalse() {

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenAnswer(returnsFirstArg());

        Booking result = bookingService.approve(USER_ID, BOOKING_ID, false);

        assertNotNull(result);
        assertThat(result.getStatus(), equalTo(BookingStatus.REJECTED));
    }

    @Test
    void approve_shouldThrowNotFoundExceptionWhenUserIsNotItemOwner() {

        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class, () -> bookingService.approve(USER_ID_WRONG, anyLong(), true));
    }

    @Test
    void getAllByBooker_shouldReturnListOfOneBooking() {

        when(userService.getById(USER_ID)).thenReturn(user);
        when(bookingRepository.findAllByBooker_IdOrderByStartDesc(USER_ID))
                .thenReturn(List.of(booking));
        when(bookingMapper.toBookingDtoOutput(booking))
                .thenReturn(bookingDto);

        List<BookingDto> bookings = bookingService
                .getAllByBooker(USER_ID, BookingState.FUTURE, null, null);

        assertNotNull(bookings);
        assertThat(bookings, hasSize(1));
    }

    @Test
    void getAllByItemsOwner_shouldReturnListOfOneBooking() {

        when(userService.getById(USER_ID)).thenReturn(user);
        when(itemRepository.findAllByOwnerId(USER_ID)).thenReturn(List.of(item));
        when(bookingRepository.findAll()).thenReturn(List.of(booking));
        when(bookingMapper.toBookingDtoOutput(booking))
                .thenReturn(bookingDto);

        List<BookingDto> bookingsDto = bookingService
                .getAllByItemsOwner(USER_ID, BookingState.FUTURE, null, null);

        assertNotNull(bookingsDto);
        assertThat(bookingsDto, hasSize(1));
    }

    @Test
    void getAllByItemsOwner_shouldNotFoundExceptionWhenUserHasNoItems() {

        when(userService.getById(USER_ID)).thenReturn(user);
        when(itemRepository.findAllByOwnerId(USER_ID)).thenReturn(Collections.emptyList());

        assertThrows(NotFoundException.class,
                () -> bookingService.getAllByItemsOwner(USER_ID, BookingState.FUTURE, null, null));
    }

    @Test
    void getAllByItem_shouldReturnListOfOneBooking() {

        when(bookingRepository.findBookingsByItem(any())).thenReturn(List.of(booking));

        List<Booking> bookings = bookingService.getAllByItem(item);

        assertNotNull(bookings);
        assertThat(bookings, hasSize(1));
    }
}