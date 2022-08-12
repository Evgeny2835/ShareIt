package ru.practicum.shareit.booking.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class Booking {
    private Long id;
    private LocalDate start;
    private LocalDate end;
    private String item;
    private String booker;
    private Status status;
}