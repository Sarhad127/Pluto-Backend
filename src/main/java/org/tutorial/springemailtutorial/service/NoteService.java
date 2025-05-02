package org.tutorial.springemailtutorial.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tutorial.springemailtutorial.model.Note;
import org.tutorial.springemailtutorial.model.User;
import org.tutorial.springemailtutorial.repository.NoteRepository;
import org.tutorial.springemailtutorial.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    public List<Note> getNotes(String authHeader) {
        String username = extractUsernameFromToken(authHeader);
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found.");
        }
        return noteRepository.findByUser(userOptional.get());
    }

    public Note createNote(Note note, String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }
        String token = authHeader.substring(7).trim();
        String username = jwtService.extractUsername(token);
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found.");
        }
        User user = userOptional.get();
        if (note.getTitle() == null || note.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Note title cannot be empty.");
        }
        note.setUser(user);
        note.setTitle(note.getTitle().trim());
        note.setColor(note.getColor().trim());
        note.setDate(note.getDate());
        note.setText(note.getText());
        System.out.println("Creating note: " + note);
        return noteRepository.save(note);
    }

    public Note updateNote(Long id, Note note, String authHeader) {
        String username = extractUsernameFromToken(authHeader);
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found.");
        }

        Optional<Note> existingNote = noteRepository.findById(id);
        if (existingNote.isEmpty()) {
            throw new RuntimeException("Note not found.");
        }

        Note updatedNote = existingNote.get();
        if (note.getTitle() != null && !note.getTitle().trim().isEmpty()) {
            updatedNote.setTitle(note.getTitle());
        }
        return noteRepository.save(updatedNote);
    }

    public void deleteNote(Long id, String authHeader) {
        String username = extractUsernameFromToken(authHeader);
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found.");
        }

        Optional<Note> noteOptional = noteRepository.findById(id);
        if (noteOptional.isEmpty()) {
            throw new RuntimeException("Note not found.");
        }

        Note note = noteOptional.get();
        if (!note.getUser().getUsername().equals(username)) {
            throw new SecurityException("User not authorized to delete this note.");
        }

        noteRepository.delete(note);
    }

    private String extractUsernameFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }
        String token = authHeader.substring(7).trim();
        return jwtService.extractUsername(token);
    }
}
