package org.tutorial.springemailtutorial.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tutorial.springemailtutorial.model.Note;
import org.tutorial.springemailtutorial.service.NoteService;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @GetMapping
    public ResponseEntity<List<Note>> getNotes(@RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(noteService.getNotes(authHeader));
    }

    @PostMapping
    public ResponseEntity<Note> createNote(@RequestBody Note note,
                                           @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(noteService.createNote(note, authHeader));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Note> updateNote(@PathVariable Long id,
                                           @RequestBody Note note,
                                           @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(noteService.updateNote(id, note, authHeader));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id,
                                           @RequestHeader("Authorization") String authHeader) {
        noteService.deleteNote(id, authHeader);
        return ResponseEntity.noContent().build();
    }
}
