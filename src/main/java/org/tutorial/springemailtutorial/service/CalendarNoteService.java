package org.tutorial.springemailtutorial.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tutorial.springemailtutorial.dto.CalendarNoteDTO;
import org.tutorial.springemailtutorial.model.CalendarNote;
import org.tutorial.springemailtutorial.model.User;
import org.tutorial.springemailtutorial.repository.CalendarNoteRepository;
import org.tutorial.springemailtutorial.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class CalendarNoteService {

    @Autowired
    private CalendarNoteRepository calendarNoteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    public CalendarNote saveOrUpdateNote(String authHeader, CalendarNoteDTO noteDTO) {
        String username = jwtService.extractUsername(authHeader.substring(7));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        LocalDate date = noteDTO.getDate();
        String content = noteDTO.getContent();
        String color = noteDTO.getColor();
        String textColor = noteDTO.getTextColor();
        String title = noteDTO.getTitle();
        LocalTime timeFrom = noteDTO.getTimeFrom();
        LocalTime timeTo = noteDTO.getTimeTo();

        Optional<CalendarNote> existingNote = calendarNoteRepository.findByUserAndDate(user, date);
        if (existingNote.isPresent()) {
            CalendarNote note = existingNote.get();
            if (content == null || content.trim().isEmpty()) {
                calendarNoteRepository.delete(note);
                return null;
            }
            note.setContent(content);
            note.setColor(color);
            note.setTextColor(textColor);
            note.setTitle(title);
            note.setTimeFrom(timeFrom);
            note.setTimeTo(timeTo);
            return calendarNoteRepository.save(note);
        } else {
            if (content != null && !content.trim().isEmpty()) {
                CalendarNote newNote = new CalendarNote(date, content, user);
                newNote.setColor(color);
                return calendarNoteRepository.save(newNote);
            }
            return null;
        }
    }

    public List<CalendarNote> getUserNotes(String authHeader) {
        String username = jwtService.extractUsername(authHeader.substring(7));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return calendarNoteRepository.findByUser(user);
    }

    public void deleteNoteByDate(String authHeader, LocalDate date) {
        String username = jwtService.extractUsername(authHeader.substring(7));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        CalendarNote note = calendarNoteRepository.findByUserAndDate(user, date)
                .orElseThrow(() -> new RuntimeException("Note not found"));
        calendarNoteRepository.delete(note);
    }
}
