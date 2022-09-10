package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.service.CommentMapper;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTest {
    static final String URL = "/items";
    static final Long USER_ID = 1L;
    static final Long USER_ID_WRONG = 5L;
    static final String USER_NAME = "user";
    static final String USER_EMAIL = "user@yandex.ru";
    static final Long ITEM_ID = 1L;
    static final String ITEM_NAME = "item";
    static final String ITEM_NAME_UPDATE = "item_update";
    static final String ITEM_DESCRIPTION = "item_description";
    @MockBean
    ItemService itemService;
    @MockBean
    ItemMapper itemMapper;
    @MockBean
    CommentMapper commentMapper;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    final User user = new User(USER_ID, USER_NAME, USER_EMAIL);
    final ItemDto itemDto = ItemDto.builder()
            .id(ITEM_ID)
            .name(ITEM_NAME)
            .description(ITEM_DESCRIPTION)
            .available(true)
            .ownerId(USER_ID)
            .build();

    @Test
    void create_shouldReturnItemWithStatus200() throws Exception {
        when(itemService.create(any(), any())).thenReturn(itemDto);

        mockMvc.perform(post(URL)
                        .header("X-Sharer-User-Id", USER_ID)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.ownerId", is(itemDto.getOwnerId()), Long.class));
    }

    @Test
    void create_shouldAnswer404WithWrongUserId() throws Exception {
        when(itemService.create(any(), any())).thenThrow(NotFoundException.class);

        mockMvc.perform(post(URL)
                        .header("X-Sharer-User-Id", USER_ID)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().is(404));
    }

    @Test
    void update_shouldUpdateItemNameWithStatus200() throws Exception {
        final ItemDto itemDto = ItemDto.builder()
                .id(ITEM_ID)
                .name(ITEM_NAME_UPDATE)
                .description(ITEM_DESCRIPTION)
                .available(true)
                .ownerId(USER_ID)
                .build();

        when(itemService.update(any(), any(), any())).thenReturn(itemDto);

        mockMvc.perform(patch(URL + "/{itemId}", ITEM_ID)
                        .header("X-Sharer-User-Id", USER_ID)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(itemDto.getName())));
    }

    @Test
    void delete_shouldDeleteItemByIdWithStatus200() throws Exception {
        mockMvc.perform(delete(URL + "/{id}", ITEM_ID)
                        .header("X-Sharer-User-Id", USER_ID))
                .andExpect(status().is(200));
        verify(itemService, times(1)).deleteItem(any(), any());
    }

    @Test
    void get_shouldReturnItemWithStatus200() throws Exception {
        when(itemMapper.toItemDto(any(), any())).thenReturn(itemDto);

        mockMvc.perform(get(URL + "/{itemId}", ITEM_ID)
                        .header("X-Sharer-User-Id", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.ownerId", is(itemDto.getOwnerId()), Long.class));
    }

    @Test
    void get_shouldAnswer404WhenWrongItemId() throws Exception {
        when(itemService.getById(ITEM_ID)).thenThrow(NotFoundException.class);

        mockMvc.perform(get(URL + "/{itemId}", ITEM_ID)
                        .header("X-Sharer-User-Id", USER_ID))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserItems_shouldReturnListOfOneUserWithStatus200() throws Exception {
        when(itemService.getUserItems(any(), any(), any()))
                .thenReturn(List.of(itemDto));

        mockMvc.perform(get(URL)
                        .header("X-Sharer-User-Id", USER_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$[0].available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$[0].ownerId", is(itemDto.getOwnerId()), Long.class));
    }

    @Test
    void getUserItems_shouldAnswer404WithWrongUserId() throws Exception {
        when(itemService.getUserItems(any(), any(), any())).thenThrow(NotFoundException.class);

        mockMvc.perform(get(URL)
                        .header("X-Sharer-User-Id", USER_ID_WRONG))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void keywordSearch_shouldReturnItemsWithGivenString() throws Exception {
        when(itemService.keywordSearch(any(), any(), any())).thenReturn(List.of(itemDto));

        mockMvc.perform(get(URL + "/search").param("text", "one"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)));
    }

    @Test
    void createComment_shouldAddCommentAndReturn() throws Exception {
        CommentDto commentDto = CommentDto.builder()
                .id(1L)
                .text("comment")
                .authorName(user.getName())
                .created(LocalDateTime.now().minusSeconds(60))
                .build();

        when(commentMapper.toCommentDto(any())).thenReturn(commentDto);

        mockMvc.perform(post(URL + "/{itemId}" + "/comment", ITEM_ID)
                        .header("X-Sharer-User-Id", USER_ID)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(commentDto.getText())));
    }
}
