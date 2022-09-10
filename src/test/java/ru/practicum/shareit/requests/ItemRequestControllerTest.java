package ru.practicum.shareit.requests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.requests.controller.ItemRequestController;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestControllerTest {
    @MockBean
    ItemRequestService itemRequestService;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    static final String URL = "/requests";
    private static final Long USER_ID = 1L;
    private static final Long USER_ID_WRONG = 5L;
    private static final Long REQUEST_ID = 1L;
    private static final String REQUEST_DESCRIPTION = "request_description";
    private static final LocalDateTime CREATED = LocalDateTime.now();
    final User user = User.builder()
            .id(USER_ID)
            .name("user_name")
            .email("user_email@yandex.ru")
            .build();
    final ItemRequestDto requestCreateDto = ItemRequestDto.builder()
            .description(REQUEST_DESCRIPTION)
            .build();
    final ItemRequestDto requestDto = ItemRequestDto.builder()
            .id(REQUEST_ID)
            .description(REQUEST_DESCRIPTION)
            .created(CREATED)
            .ownerId(user.getId())
            .items(Collections.emptyList())
            .build();

    @Test
    void create_shouldAnswer200AndReturnRequest() throws Exception {

        when(itemRequestService.create(any(), any())).thenReturn(requestDto);

        mockMvc.perform(post(URL)
                        .header("X-Sharer-User-Id", USER_ID)
                        .content(objectMapper.writeValueAsString(requestCreateDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.id", is(requestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(requestDto.getDescription())))
                .andExpect(jsonPath("$.created", is(requestDto.getCreated().toString())))
                .andExpect(jsonPath("$.ownerId", is(requestDto.getOwnerId()), Long.class))
                .andExpect(jsonPath("$.items", is(requestDto.getItems())));
    }

    @Test
    void create_shouldAnswer404WhenOwnerIdNotFound() throws Exception {

        when(itemRequestService.create(any(), any())).thenThrow(NotFoundException.class);

        mockMvc.perform(post(URL)
                        .header("X-Sharer-User-Id", USER_ID)
                        .content(objectMapper.writeValueAsString(requestCreateDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
    }

    @Test
    void getByUser_shouldAnswer200AndReturnListOfOneUserRequestWhenUserIsItemOwner() throws Exception {

        when(itemRequestService.getByUser(USER_ID)).thenReturn(List.of(requestDto));

        mockMvc.perform(get(URL)
                        .header("X-Sharer-User-Id", USER_ID))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(requestDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(requestDto.getDescription())))
                .andExpect(jsonPath("$[0].created", is(requestDto.getCreated().toString())))
                .andExpect(jsonPath("$[0].ownerId", is(requestDto.getOwnerId()), Long.class))
                .andExpect(jsonPath("$[0].items", is(requestDto.getItems())));
    }

    @Test
    void getByUser_shouldAnswer404IfWrongUser() throws Exception {

        when(itemRequestService.getByUser(USER_ID_WRONG)).thenThrow(NotFoundException.class);

        mockMvc.perform(get(URL)
                        .header("X-Sharer-User-Id", USER_ID_WRONG))
                .andExpect(status().is(404));
    }

    @Test
    void getAll_shouldAnswer200AndReturnEmptyRequestsIfRequestOwnerIsAnotherUserAndWithoutParams() throws Exception {

        when(itemRequestService.getAll(USER_ID, null, null)).thenReturn(Collections.emptyList());

        mockMvc.perform(get(URL + "/all")
                        .header("X-Sharer-User-Id", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(0)));
    }

    @Test
    void getAll_shouldAnswer200AndReturnListOfOneRequestIfRequestOwnerIsAnotherUserAndWithParams() throws Exception {
        User userTwo = User.builder()
                .id(2L)
                .name("user_two_name")
                .email("user_two_email@yandex.ru")
                .build();

        when(itemRequestService.getAll(any(), any(), any())).thenReturn(List.of(requestDto));

        mockMvc.perform(get(URL + "/all")
                        .header("X-Sharer-User-Id", userTwo.getId())
                        .param("from", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(1)));
    }

    @Test
    void getById_shouldAnswer200AndReturnRequest() throws Exception {
        when(itemRequestService.getById(USER_ID, REQUEST_ID)).thenReturn(requestDto);

        mockMvc.perform(get(URL + "/{requestId}", REQUEST_ID)
                        .header("X-Sharer-User-Id", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(requestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(requestDto.getDescription())))
                .andExpect(jsonPath("$.created", is(requestDto.getCreated().toString())))
                .andExpect(jsonPath("$.ownerId", is(requestDto.getOwnerId()), Long.class))
                .andExpect(jsonPath("$.items", is(requestDto.getItems())));
    }
}