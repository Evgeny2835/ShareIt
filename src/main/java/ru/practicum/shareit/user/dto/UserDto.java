package ru.practicum.shareit.user.dto;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserDto {
    private Long id;
    @NotEmpty
    private String name;
    @NotEmpty
    @Email
    private String email;
}