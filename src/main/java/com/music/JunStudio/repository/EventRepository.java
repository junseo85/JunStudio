package com.music.JunStudio.repository;

import com.music.JunStudio.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Integer> {
}
