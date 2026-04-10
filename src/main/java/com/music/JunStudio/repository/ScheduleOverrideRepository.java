package com.music.JunStudio.repository;

import com.music.JunStudio.model.ScheduleOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ScheduleOverrideRepository extends JpaRepository<ScheduleOverride, Long> {
    Optional<ScheduleOverride> findByOverrideDate(LocalDate date);
}