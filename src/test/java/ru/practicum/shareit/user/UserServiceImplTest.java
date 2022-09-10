package ru.practicum.shareit.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.EmailDuplicateException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    static final Long USER_ID = 1L;
    static final String NAME = "Maks";
    static final String UPDATE_NAME = "Maks_updated";
    static final String EMAIL = "maks@yandex.ru";
    @Mock
    UserRepository userRepository;
    UserService userService;
    final UserDto userDto = new UserDto(USER_ID, NAME, EMAIL);
    final UserDto userDtoUpdate = new UserDto(USER_ID, UPDATE_NAME, EMAIL);
    final User user = new User(USER_ID, NAME, EMAIL);

    @BeforeEach
    void init() {
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void create_shouldReturnNewUser() {
        when(userRepository.save(any())).thenReturn(user);

        UserDto result = userService.create(userDto);

        verify(userRepository, times(1)).save(any());
        assertNotNull(result);
        assertThat(result.getId(), equalTo(user.getId()));
        assertThat(result.getName(), equalTo(user.getName()));
        assertThat(result.getEmail(), equalTo(user.getEmail()));
    }

    @Test
    void create_shouldThrowEmailDuplicateException() {
        when(userRepository.save(any(User.class)))
                .thenThrow(new EmailDuplicateException("Duplicate email"));

        final EmailDuplicateException exception = Assertions.assertThrows(
                EmailDuplicateException.class,
                () -> userService.create(userDto));

        verify(userRepository, times(1)).save(any(User.class));
        Assertions.assertEquals("Duplicate email", exception.getMessage());
    }

    @Test
    void update_shouldUpdateUserName() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(returnsFirstArg());

        UserDto result = userService.update(userDtoUpdate, USER_ID);

        assertNotNull(result);
        assertThat(result.getName(), equalTo(UPDATE_NAME));
        verify(userRepository, times(1)).findById(USER_ID);
        verify(userRepository, times((1))).save(any(User.class));

    }

    @Test
    void update_shouldThrowNotFoundExceptionIfRepositoryIsEmpty() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        final NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.update(userDtoUpdate, USER_ID));

        Assertions.assertEquals("User not found: id=1", exception.getMessage());
        verify(userRepository, times(1)).findById(USER_ID);
    }

    @Test
    void update_shouldThrowDuplicateEmailException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenThrow(new EmailDuplicateException("Email exists"));

        final EmailDuplicateException exception = assertThrows(
                EmailDuplicateException.class,
                () -> userService.update(userDtoUpdate, USER_ID));

        Assertions.assertEquals("Email exists", exception.getMessage());
        verify(userRepository, times(1)).findById(USER_ID);
        verify(userRepository, times((1))).save(any(User.class));
    }

    @Test
    void delete_shouldInvokeRepositoryDeleteOneTime() {
        userRepository.deleteById(USER_ID);
        verify(userRepository, times(1)).deleteById(USER_ID);
    }

    @Test
    void delete_shouldThrowNotFoundExceptionIfInvokeRepositoryWithWrongUserId() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        final NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.deleteUser(USER_ID));

        Assertions.assertEquals(String.format("User not found: id=%d", USER_ID), exception.getMessage());
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void getById_shouldReturnUser() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        User result = userService.getById(USER_ID);

        assertNotNull(result);
        assertThat(result.getId(), equalTo(user.getId()));
        assertThat(result.getName(), equalTo(user.getName()));
        assertThat(result.getEmail(), equalTo(user.getEmail()));
        verify(userRepository, times(1)).findById(USER_ID);
    }

    @Test
    void getById_throwNotFoundExceptionIfInvokeRepositoryWithWrongUserId() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        final NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.getById(USER_ID));

        Assertions.assertEquals(String.format("User not found: id=%d", USER_ID), exception.getMessage());
        verify(userRepository, times(1)).findById(USER_ID);
    }

    @Test
    void getUsers_shouldReturnUserList() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<User> result = userService.getUsers();

        verify(userRepository, times(1)).findAll();
        assertNotNull(result);
        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), equalTo(user.getId()));
        assertThat(result.get(0).getName(), equalTo(user.getName()));
        assertThat(result.get(0).getEmail(), equalTo(user.getEmail()));
    }
}