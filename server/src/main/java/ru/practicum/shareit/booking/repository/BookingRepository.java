package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> getBookingsByBookerId(Long bookerId);

    List<Booking> findAllByBooker_IdOrderByStartDesc(Long bookerId);

    List<Booking> getBookingsByBookerId(Long bookerId, Pageable pageable);

    List<Booking> findBookingsByItem(Item item);

    List<Booking> findBookingsByItem_Id(Long itemId);

    Page<Booking> findAllByItemIn(Collection<Item> item, Pageable pageable);
}