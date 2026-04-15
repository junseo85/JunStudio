package com.music.JunStudio.repository;

import com.music.JunStudio.model.ScheduleOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleOverrideRepository extends JpaRepository<ScheduleOverride, Long> {

    // CHANGED: Returns a List instead of Optional
    List<ScheduleOverride> findByOverrideDate(LocalDate overrideDate);
    // Finds overrides belonging to this teacher, OR where teacher is null (Global holidays)
    List<ScheduleOverride> findByTeacherIdOrTeacherIsNull(Long teacherId);
}