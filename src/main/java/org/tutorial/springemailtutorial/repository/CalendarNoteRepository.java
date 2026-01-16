package org.tutorial.springemailtutorial.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tutorial.springemailtutorial.model.CalendarNote;
import org.tutorial.springemailtutorial.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CalendarNoteRepository extends JpaRepository<CalendarNote, Long> {
    List<CalendarNote> findByUser(User user);
    Optional<CalendarNote> findByUserAndDate(User user, LocalDate date);
}
