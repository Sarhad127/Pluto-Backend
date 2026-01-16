package org.tutorial.springemailtutorial.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tutorial.springemailtutorial.dto.CalendarNoteDTO;
import org.tutorial.springemailtutorial.model.CalendarNote;
import org.tutorial.springemailtutorial.service.CalendarNoteService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/calendar")
public class CalendarNoteController {

    @Autowired
    private CalendarNoteService calendarNoteService;

    @PostMapping
    public ResponseEntity<CalendarNote> saveOrUpdateNote(@RequestBody CalendarNoteDTO noteDTO, @RequestHeader("Authorization") String authHeader) {
        CalendarNote savedNote = calendarNoteService.saveOrUpdateNote(authHeader, noteDTO);
        return ResponseEntity.ok(savedNote);
    }

    @GetMapping
    public ResponseEntity<List<CalendarNote>> getUserNotes(@RequestHeader("Authorization") String authHeader) {
        List<CalendarNote> notes = calendarNoteService.getUserNotes(authHeader);
        return ResponseEntity.ok(notes);
    }

    @DeleteMapping("/{date}")
    public ResponseEntity<Void> deleteNoteByDate(@PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                 @RequestHeader("Authorization") String authHeader) {
        calendarNoteService.deleteNoteByDate(authHeader, date);
        return ResponseEntity.noContent().build();
    }
}
