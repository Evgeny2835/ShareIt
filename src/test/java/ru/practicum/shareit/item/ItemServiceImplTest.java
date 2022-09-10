package ru.practicum.shareit.item;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.comment.repository.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.service.ItemServiceImpl;
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
public class ItemServiceImplTest {
    static final Long USER_ID = 1L;
    static final Long USER_ID_WRONG = 5L;
    static final String USER_NAME = "user";
    static final String USER_EMAIL = "user@yandex.ru";
    static final Long ITEM_ID = 1L;
    static final String ITEM_NAME = "item";
    static final String ITEM_NAME_UPDATE = "item_update";
    static final String ITEM_DESCRIPTION = "item_description";
    final User user = new User(USER_ID, USER_NAME, USER_EMAIL);
    final Item item = Item.builder()
            .id(ITEM_ID)
            .name(ITEM_NAME)
            .description(ITEM_DESCRIPTION)
            .available(true)
            .owner(user)
            .build();
    final ItemDto itemDto = ItemDto.builder()
            .id(ITEM_ID)
            .name(ITEM_NAME)
            .description(ITEM_DESCRIPTION)
            .available(true)
            .ownerId(USER_ID)
            .build();
    @Mock
    ItemRepository itemRepository;
    @Mock
    UserService userService;
    @Mock
    ItemMapper itemMapper;
    @Mock
    CommentRepository commentRepository;
    @Mock
    BookingRepository bookingRepository;
    ItemService itemService;

    @BeforeEach
    void init() {
        itemService = new ItemServiceImpl(itemRepository, userService,
                itemMapper, commentRepository, bookingRepository);
    }

    @Test
    void create_shouldReturnNewItem() {
        when(userService.getById(anyLong())).thenReturn(user);
        when(itemMapper.toItem(any(ItemDto.class), any(User.class))).thenReturn(item);
        when(itemRepository.save(any(Item.class))).thenAnswer(returnsFirstArg());
        when(itemMapper.toItemDto(any(Item.class))).thenReturn(itemDto);

        ItemDto result = itemService.create(USER_ID, itemDto);

        verify(itemRepository, times(1)).save(item);
        assertNotNull(result);
        assertThat(result.getId(), equalTo(item.getId()));
        assertThat(result.getName(), equalTo(item.getName()));
        assertThat(result.getDescription(), equalTo(item.getDescription()));
        assertThat(result.getAvailable(), equalTo(item.getAvailable()));
        assertThat(result.getOwnerId(), equalTo(item.getOwner().getId()));
    }

    @Test
    void update_shouldUpdateItemName() {
        ItemDto itemDto = ItemDto.builder()
                .id(ITEM_ID)
                .name(ITEM_NAME_UPDATE)
                .description(ITEM_DESCRIPTION)
                .available(true)
                .ownerId(USER_ID)
                .build();

        when(userService.getById(anyLong())).thenReturn(user);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(itemRepository.save(any())).thenAnswer(returnsFirstArg());
        when(itemMapper.toItemDto(any())).thenReturn(itemDto);

        ItemDto result = itemService.update(USER_ID, itemDto, ITEM_ID);

        verify(userService, times(1)).getById(anyLong());
        verify(itemRepository, times(1)).findById(ITEM_ID);
        verify(itemRepository, times(1)).save(item);
        assertNotNull(result);
        assertThat(result.getId(), equalTo(item.getId()));
        assertThat(result.getName(), equalTo(item.getName()));
        assertThat(result.getDescription(), equalTo(item.getDescription()));
        assertThat(result.getAvailable(), equalTo(item.getAvailable()));
        assertThat(result.getOwnerId(), equalTo(item.getOwner().getId()));
    }

    @Test
    void update_shouldThrowNotFoundExceptionIfWrongOwner() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> itemService.update(USER_ID_WRONG, itemDto, ITEM_ID));

        verify(itemRepository, times(1)).findById(anyLong());
        Assertions.assertEquals(String.format("User id=%d is not the owner item id=%d",
                USER_ID_WRONG, ITEM_ID), exception.getMessage());
    }

    @Test
    void update_shouldThrowNotFoundExceptionIfItemNotFound() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> itemService.update(USER_ID, itemDto, ITEM_ID));

        verify(itemRepository, times(1)).findById(anyLong());
        Assertions.assertEquals(String.format("Item not found: id=%d", ITEM_ID), exception.getMessage());
    }

    @Test
    void delete_shouldDeleteUser() {
        itemRepository.deleteById(ITEM_ID);
        verify(itemRepository, times(1)).deleteById(ITEM_ID);
    }

    @Test
    void delete_shouldThrowNotFoundExceptionIfWrongItem_Id() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> itemService.deleteItem(USER_ID_WRONG, ITEM_ID));

        verify(itemRepository, times(1)).findById(ITEM_ID);
        Assertions.assertEquals(String.format("User id=%d is not the owner item id=%d",
                USER_ID_WRONG, ITEM_ID), exception.getMessage());
    }

    @Test
    void delete_shouldThrowNotFoundExceptionIfUserNotFound() {
        String errorMessage = String.format("User not found: id=%d", USER_ID);
        when(userService.getById(USER_ID)).thenThrow(new NotFoundException(errorMessage));

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> itemService.deleteItem(USER_ID, ITEM_ID));

        verify(userService, times(1)).getById(USER_ID);
        Assertions.assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void getById_shouldReturnItem() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));

        Item result = itemService.getById(ITEM_ID);

        verify(itemRepository, times(1)).findById(ITEM_ID);
        assertNotNull(result);
        assertThat(result.getId(), equalTo(item.getId()));
        assertThat(result.getName(), equalTo(item.getName()));
        assertThat(result.getDescription(), equalTo(item.getDescription()));
        assertThat(result.getAvailable(), equalTo(item.getAvailable()));
        assertThat(result.getOwner(), equalTo(item.getOwner()));
    }

    @Test
    void getById_throwNotFoundExceptionIfWrongItemId() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.empty());

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> itemService.getById(ITEM_ID));

        verify(itemRepository, times(1)).findById(ITEM_ID);
        Assertions.assertEquals(String.format("Item not found: id=%d", ITEM_ID), exception.getMessage());
    }

    @Test
    void getUserItems_shouldReturnListOfOneItem() {
        when(userService.getById(USER_ID)).thenReturn(user);
        when(itemRepository.findAllByOwnerId(USER_ID)).thenReturn(List.of(item));
        when(itemMapper.toItemDto(item)).thenReturn(itemDto);

        List<ItemDto> result = itemService.getUserItems(USER_ID, null, null);

        verify(userService, times(1)).getById(USER_ID);
        verify(itemRepository, times(1)).findAllByOwnerId(USER_ID);
        verify(itemMapper, times(1)).toItemDto(item);

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), equalTo(item.getId()));
        assertThat(result.get(0).getName(), equalTo(item.getName()));
        assertThat(result.get(0).getDescription(), equalTo(item.getDescription()));
        assertThat(result.get(0).getAvailable(), equalTo(item.getAvailable()));
        assertThat(result.get(0).getOwnerId(), equalTo(item.getOwner().getId()));
    }

    @Test
    void keywordSearch_shouldReturnListOfOneItem() {
        when(itemRepository.search(any())).thenReturn(List.of(item));
        when(itemMapper.toItemDto(any())).thenReturn(itemDto);

        List<ItemDto> result = itemService.keywordSearch("test", null, null);

        verify(itemRepository, times(1)).search(any());
        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), equalTo(item.getId()));
    }

    @Test
    void createComment_shouldSaveAndReturnComment() {
        when(userService.getById(USER_ID)).thenReturn(user);
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));

        CommentDto commentDto = CommentDto.builder()
                .text("comment")
                .authorName(USER_NAME)
                .build();

        Booking booking = Booking.builder()
                .item(item)
                .booker(user)
                .start(LocalDateTime.now().minusSeconds(120))
                .end(LocalDateTime.now().minusSeconds(60))
                .build();

        when(bookingRepository.getBookingsByBookerId(any())).thenReturn(List.of(booking));
        when(commentRepository.save(any())).thenAnswer(returnsFirstArg());

        Comment result = itemService.createComment(USER_ID, ITEM_ID, commentDto);

        verify(commentRepository, times(1)).save(any());
        assertNotNull(result);
        assertThat(commentDto.getText(), equalTo(result.getText()));
    }
}