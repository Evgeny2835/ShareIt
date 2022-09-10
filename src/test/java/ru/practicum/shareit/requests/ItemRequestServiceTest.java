package ru.practicum.shareit.requests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.mapper.ItemRequestMapper;
import ru.practicum.shareit.requests.model.ItemRequest;
import ru.practicum.shareit.requests.repository.ItemRequestRepository;
import ru.practicum.shareit.requests.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceTest {
    @Mock
    UserService userService;
    @Mock
    ItemRequestMapper requestMapper;
    @Mock
    ItemRequestRepository requestRepository;
    @InjectMocks
    ItemRequestServiceImpl requestService;
    private static final Long USER_ID = 1L;
    private static final Long USER_ID_WRONG = 5L;
    private static final Long REQUEST_ID = 1L;
    private static final Long REQUEST_ID_WRONG = 3L;
    private static final String REQUEST_DESCRIPTION = "request_description";
    private static final LocalDateTime CREATED = LocalDateTime.now();
    final User user = User.builder()
            .id(USER_ID)
            .name("user_name")
            .email("user_email@yandex.ru")
            .build();
    final ItemRequest request = ItemRequest.builder()
            .id(REQUEST_ID)
            .description(REQUEST_DESCRIPTION)
            .created(CREATED)
            .owner(user)
            .items(Collections.emptyList())
            .build();
    ItemRequestDto requestCreateDto = ItemRequestDto.builder()
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
    void create_shouldCreateRequest() {

        when(userService.getById(USER_ID)).thenReturn(user);
        when(requestMapper.toItemRequest(requestCreateDto, user)).thenReturn(request);
        when(requestRepository.save(request)).thenAnswer(returnsFirstArg());
        when(requestMapper.toItemRequestDto(request)).thenReturn(requestDto);

        ItemRequestDto result = requestService.create(USER_ID, requestCreateDto);

        assertNotNull(result);
        assertThat(result.getId(), equalTo(requestDto.getId()));
        assertThat(result.getDescription(), equalTo(requestDto.getDescription()));
        assertThat(result.getCreated(), equalTo(requestDto.getCreated()));
        assertThat(result.getOwnerId(), equalTo(requestDto.getOwnerId()));
        assertThat(result.getItems(), equalTo(requestDto.getItems()));
    }

    @Test
    void getByUser_shouldReturnListOfOneUserRequestWhenUserIsItemOwner() {

        when(userService.getById(USER_ID)).thenReturn(user);
        when(requestRepository.findAllByOwnerId(USER_ID)).thenReturn(List.of(request));
        when(requestMapper.toItemRequestDto(request)).thenReturn(requestDto);

        List<ItemRequestDto> result = requestService.getByUser(USER_ID);

        assertNotNull(result);
        assertThat(result.get(0).getId(), equalTo(requestDto.getId()));
        assertThat(result.get(0).getDescription(), equalTo(requestDto.getDescription()));
        assertThat(result.get(0).getCreated(), equalTo(requestDto.getCreated()));
        assertThat(result.get(0).getOwnerId(), equalTo(requestDto.getOwnerId()));
        assertThat(result.get(0).getItems(), equalTo(requestDto.getItems()));
    }

    @Test
    void getByUser_shouldThrowNotFoundExceptionIfWrongUser() {

        when(userService.getById(USER_ID_WRONG)).thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> requestService.getByUser(USER_ID_WRONG));
    }

    @Test
    void getAll_shouldReturnEmptyListOfRequestsWhenRequestOwnerIsAnotherUser() {

        when(requestRepository.findAllByOwnerIdIsNot(USER_ID)).thenReturn(Collections.emptyList());

        List<ItemRequestDto> result = requestService.getAll(USER_ID, null, null);

        assertThat(result, hasSize(0));
    }

    @Test
    void getById_shouldSaveAndReturnRequest() {

        when(requestRepository.findById(REQUEST_ID)).thenReturn(Optional.of(request));
        when(requestMapper.toItemRequestDto(request)).thenReturn(requestDto);

        ItemRequestDto result = requestService.getById(USER_ID, REQUEST_ID);

        assertNotNull(result);
        assertThat(result.getId(), equalTo(requestDto.getId()));
        assertThat(result.getDescription(), equalTo(requestDto.getDescription()));
        assertThat(result.getCreated(), equalTo(requestDto.getCreated()));
        assertThat(result.getOwnerId(), equalTo(requestDto.getOwnerId()));
        assertThat(result.getItems(), equalTo(requestDto.getItems()));
    }

    @Test
    void getById_shouldThrowNotFoundExceptionWhenRequestNotFound() {
        when(requestRepository.findById(REQUEST_ID_WRONG)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> requestService.getById(USER_ID, REQUEST_ID_WRONG));
    }

    @Test
    void getById_shouldThrowNotFoundExceptionWhenUserServiceThrowNotFoundException() {
        when(userService.getById(anyLong())).thenThrow(NotFoundException.class);

        assertThrows(NotFoundException.class, () -> requestService.getById(USER_ID, REQUEST_ID_WRONG));
    }
}