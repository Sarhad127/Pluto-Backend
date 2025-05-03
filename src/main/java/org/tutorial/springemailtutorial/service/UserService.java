package org.tutorial.springemailtutorial.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.tutorial.springemailtutorial.model.User;
import org.tutorial.springemailtutorial.repository.BoardRepository;
import org.tutorial.springemailtutorial.repository.CalendarNoteRepository;
import org.tutorial.springemailtutorial.repository.NoteRepository;
import org.tutorial.springemailtutorial.repository.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BoardRepository boardRepository;
    private final CalendarNoteRepository calendarNoteRepository;
    private final NoteRepository noteRepository;
    private final JwtService jwtService;

    public void updateUsername(String currentEmail, String newUsername) {
        User user = userRepository.findByUsername(currentEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (newUsername.equals(currentEmail)) {
            throw new IllegalArgumentException("New username cannot be the same as the current one.");
        }
        if (userRepository.existsByUsername(newUsername)) {
            throw new IllegalArgumentException("Username already taken.");
        }
        user.setUsername(newUsername);
        userRepository.save(user);
    }

    public void changePassword(String username, String currentPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }
        String token = authHeader.substring(7).trim();
        String username = jwtService.extractUsername(token);
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }
        User existingUser = user.get();
        boardRepository.deleteAll(existingUser.getBoards());
        calendarNoteRepository.deleteAll(existingUser.getCalendarNotes());
        noteRepository.deleteAll(existingUser.getNotes());
        userRepository.delete(existingUser);
    }
}
