package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@AllArgsConstructor
public class UserDto {
    private Long id;
    @NotEmpty
    private String name;
    @NotEmpty
    @Email
    private String email;
}