package com.music.JunStudio.repository;

import com.music.JunStudio.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Integer> {
    List<Event> findByStartDateAfterOrderByStartDateAsc(LocalDate date);

    List<Event> findByEndDateGreaterThanEqualOrderByStartDateAsc(LocalDate date);

}
