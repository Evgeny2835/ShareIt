package ru.practicum.shareit.user.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class User {
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    @Email
    private String email;

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }
}