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
import ru.practicum.shareit.item.comment.dto.CommentCreateDto;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.repository.CommentRepository;
import ru.practicum.shareit.item.comment.mapper.CommentMapper;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.service.UserService;
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
    private static final Long USER_ID = 1L;
    private static final Long USER_ID_WRONG = 5L;
    private static final String USER_NAME = "user";
    private static final String USER_EMAIL = "user@yandex.ru";
    private static final Long ITEM_ID = 1L;
    private static final String ITEM_NAME = "item";
    private static final String ITEM_NAME_UPDATE = "item_update";
    private static final String ITEM_DESCRIPTION = "item_description";
    private final User user = new User(USER_ID, USER_NAME, USER_EMAIL);
    private final Item item = Item.builder()
            .id(ITEM_ID)
            .name(ITEM_NAME)
            .description(ITEM_DESCRIPTION)
            .available(true)
            .owner(user)
            .build();
    private final ItemDto itemDto = ItemDto.builder()
            .id(ITEM_ID)
            .name(ITEM_NAME)
            .description(ITEM_DESCRIPTION)
            .available(true)
            .ownerId(USER_ID)
            .build();
    private final ItemCreateDto itemCreateDto = ItemCreateDto.builder()
            .name(ITEM_NAME)
            .description(ITEM_DESCRIPTION)
            .available(true)
            .build();
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserService userService;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentMapper commentMapper;
    private ItemService itemService;

    @BeforeEach
    void init() {
        itemService = new ItemServiceImpl(itemRepository, userService,
                itemMapper, commentRepository, bookingRepository, commentMapper);
    }

    @Test
    void create_shouldReturnNewItem() {
        when(userService.getById(anyLong())).thenReturn(user);
        when(itemMapper.toItem(any(ItemCreateDto.class), any(User.class))).thenReturn(item);
        when(itemRepository.save(any(Item.class))).thenAnswer(returnsFirstArg());
        when(itemMapper.toItemDto(any(Item.class))).thenReturn(itemDto);

        ItemDto result = itemService.create(USER_ID, itemCreateDto);

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
        when(itemRepository.findById(any())).thenThrow(NotFoundException.class);

        Assertions.assertThrows(NotFoundException.class, () ->
                itemService.deleteItem(USER_ID_WRONG, ITEM_ID));
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
    void getById_shouldReturnItemFromRepository() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));

        Item result = itemService.getById(ITEM_ID);

        assertNotNull(result);
        assertThat(result.getId(), equalTo(item.getId()));
        assertThat(result.getName(), equalTo(item.getName()));
        assertThat(result.getDescription(), equalTo(item.getDescription()));
        assertThat(result.getAvailable(), equalTo(item.getAvailable()));
        assertThat(result.getOwner().getId(), equalTo(item.getOwner().getId()));
    }

    @Test
    void getById_throwNotFoundExceptionIfWrongItemId() {
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.empty());

        final NotFoundException exception = Assertions.assertThrows(
                NotFoundException.class,
                () -> itemService.getById(ITEM_ID, USER_ID));

        verify(itemRepository, times(1)).findById(ITEM_ID);
        Assertions.assertEquals(String.format("Item not found: id=%d", ITEM_ID), exception.getMessage());
    }

    @Test
    void getUserItems_shouldReturnListOfOneItem() {
        when(itemRepository.findAllByOwnerId(any(), any())).thenReturn(List.of(item));
        when(itemMapper.toItemDto(item)).thenReturn(itemDto);

        List<ItemDto> result = itemService.getUserItems(USER_ID, 0, 10);

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), equalTo(item.getId()));
        assertThat(result.get(0).getName(), equalTo(item.getName()));
        assertThat(result.get(0).getDescription(), equalTo(item.getDescription()));
        assertThat(result.get(0).getAvailable(), equalTo(item.getAvailable()));
        assertThat(result.get(0).getOwnerId(), equalTo(item.getOwner().getId()));
    }

    @Test
    void keywordSearch_shouldReturnListOfOneItem() {
        when(itemRepository.search(any(), any())).thenReturn(List.of(item));
        when(itemMapper.toItemDto(any())).thenReturn(itemDto);

        List<ItemDto> result = itemService.keywordSearch("test", 0, 10);

        verify(itemRepository, times(1)).search(any(), any());
        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), equalTo(item.getId()));
    }

    @Test
    void createComment_shouldSaveAndReturnComment() {
        CommentCreateDto commentCreateDto = CommentCreateDto.builder().text("item_comment").build();
        CommentDto commentDto = CommentDto.builder()
                .text("item_comment")
                .authorName(USER_NAME)
                .build();

        when(userService.getById(USER_ID)).thenReturn(user);
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
        when(commentMapper.toCommentDto(any())).thenReturn(commentDto);

        Booking booking = Booking.builder()
                .item(item)
                .booker(user)
                .start(LocalDateTime.now().minusSeconds(120))
                .end(LocalDateTime.now().minusSeconds(60))
                .build();

        when(bookingRepository.getBookingsByBookerId(any())).thenReturn(List.of(booking));
        when(commentRepository.save(any())).thenAnswer(returnsFirstArg());

        CommentDto result = itemService.createComment(ITEM_ID, USER_ID, commentCreateDto);

        verify(commentRepository, times(1)).save(any());
        assertNotNull(result);
        assertThat(commentDto.getText(), equalTo(result.getText()));
    }
}