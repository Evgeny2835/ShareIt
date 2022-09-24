package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.comment.dto.CommentCreateDto;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.nio.charset.StandardCharsets;
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
    private static final String URL = "/items";
    private static final Long USER_ID = 1L;
    private static final Long USER_ID_WRONG = 5L;
    private static final Long ITEM_ID = 1L;
    private static final String ITEM_NAME = "item";
    private static final String ITEM_NAME_UPDATE = "item_update";
    private static final String ITEM_DESCRIPTION = "item_description";
    @MockBean
    private ItemService itemService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    private final ItemDto itemDto = ItemDto.builder()
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
        when(itemService.getById(any(), any())).thenReturn(itemDto);

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
        when(itemService.getById(ITEM_ID, USER_ID)).thenThrow(NotFoundException.class);

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
        CommentCreateDto commentCreateDto = CommentCreateDto.builder()
                .text("item_comment")
                .build();
        CommentDto commentDto = CommentDto.builder()
                .id(1L)
                .text("item_comment")
                .build();

        when(itemService.createComment(any(), any(), any())).thenReturn(commentDto);

        mockMvc.perform(post(URL + "/{itemId}" + "/comment", ITEM_ID)
                        .header("X-Sharer-User-Id", 2L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(commentCreateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is(commentDto.getText())));
    }

    @Test
    void createComment_shouldAnswer400WithEmptyText() throws Exception {
        CommentCreateDto commentCreateDto = CommentCreateDto.builder()
                .text("")
                .build();

        mockMvc.perform(post(URL + "/{itemId}" + "/comment", ITEM_ID)
                        .contentType("application/json")
                        .header("X-Sharer-User-Id", USER_ID)
                        .content(objectMapper.writeValueAsString(commentCreateDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}