package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.EmailDuplicateException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
public class UserControllerTest {
    private static final String URL = "/users";
    private static final Long ID = 1L;
    private static final String NAME = "test";
    private static final String UPDATED_NAME = "update";
    private static final String EMAIL = "test@yandex.ru";
    private static final String UPDATED_EMAIL = "update@yandex.ru";
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private UserService userService;
    @Autowired
    private MockMvc mockMvc;
    private final UserCreateDto userCreateDto = new UserCreateDto(NAME, EMAIL);
    private final UserDto userDto = new UserDto(ID, NAME, EMAIL);
    private final UserDto updatedUserDto = new UserDto(ID, UPDATED_NAME, UPDATED_EMAIL);

    @Test
    void create_shouldReturnUserWithStatus200() throws Exception {
        when(userService.create(any())).thenReturn(userDto);

        mockMvc.perform(post(URL)
                        .content(mapper.writeValueAsString(userCreateDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andDo(print())
                .andExpect(jsonPath("$.id", is(ID), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));
    }

    @Test
    void create_shouldThrowEmailDuplicateExceptionWithStatus409() throws Exception {
        when(userService.create(any())).thenThrow(EmailDuplicateException.class);

        mockMvc.perform(post(URL)
                        .content(mapper.writeValueAsString(userDto))
                        .contentType("application/json"))
                .andExpect(status().is(409));
    }

    @Test
    void update_shouldUpdateUserNameAndEmailWithStatus200() throws Exception {
        when(userService.update(any(), any())).thenReturn(updatedUserDto);

        mockMvc.perform(patch(URL + "/{id}", ID)
                        .content(mapper.writeValueAsString(updatedUserDto))
                        .contentType("application/json"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.name", is(updatedUserDto.getName())))
                .andExpect(jsonPath("$.email", is(updatedUserDto.getEmail())));
    }

    @Test
    void update_shoulThrowEmailDuplicateExceptionWithStatus409() throws Exception {
        when(userService.update(any(), any())).thenThrow(EmailDuplicateException.class);

        mockMvc.perform(patch(URL + "/{id}", ID)
                        .content(mapper.writeValueAsString(userDto))
                        .contentType("application/json"))
                .andExpect(status().is(409));
    }

    @Test
    void delete_shouldDeleteUserByIdWithStatus200() throws Exception {
        mockMvc.perform(delete(URL + "/{id}", ID))
                .andExpect(status().is(200));
        verify(userService, times(1)).deleteUser(ID);
    }

    @Test
    void getById_shouldReturnUserWithStatus200() throws Exception {
        User user = new User(ID, NAME, EMAIL);

        when(userService.getById(ID)).thenReturn(user);

        mockMvc.perform(get(URL + "/{id}", ID))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id", is(user.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(user.getName())))
                .andExpect(jsonPath("$.email", is(user.getEmail())));

        verify(userService, times(1)).getById(ID);
    }

    @Test
    void getById_throwNotFoundExceptionWithWrongUserId() throws Exception {
        when(userService.getById(ID)).thenThrow(NotFoundException.class);

        mockMvc.perform(get(URL + "/{id}", ID))
                .andExpect(status().is(404));

        verify(userService, times(1)).getById(ID);
    }

    @Test
    void getUsers_shouldReturnListOfTwoUsersWithStatus200() throws Exception {
        User user1 = new User(ID, NAME, EMAIL);
        User user2 = new User(2L, "Petr", "petr@mail.ru");

        when(userService.getUsers()).thenReturn(List.of(user1, user2));

        mockMvc.perform(get(URL))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[1].id", is(user2.getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(user2.getName())))
                .andExpect(jsonPath("$[1].email", is(user2.getEmail())));
    }

    @Test
    void getUsers_shouldReturnEmptyListWithStatus200() throws Exception {
        when(userService.getUsers()).thenReturn(List.of());

        mockMvc.perform(get(URL))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.length()", is(0)));

        verify(userService, times(1)).getUsers();
    }
}