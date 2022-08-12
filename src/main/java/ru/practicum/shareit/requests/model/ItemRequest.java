package ru.practicum.shareit.requests.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ItemRequest {
    private Long id;
    private String description;
    private String requestor;
    private LocalDateTime created;
}