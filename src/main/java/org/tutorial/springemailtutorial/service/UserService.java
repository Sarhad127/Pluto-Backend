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

import java.util.Random;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BoardRepository boardRepository;
    private final CalendarNoteRepository calendarNoteRepository;
    private final NoteRepository noteRepository;
    private final JwtService jwtService;

    public boolean verifyPassword(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return passwordEncoder.matches(password, user.getPassword());
    }

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
    public void deleteUser(String authHeader, String password) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }
        String token = authHeader.substring(7).trim();
        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Incorrect password");
        }
        boardRepository.deleteAll(user.getBoards());
        calendarNoteRepository.deleteAll(user.getCalendarNotes());
        noteRepository.deleteAll(user.getNotes());
        userRepository.delete(user);
    }

    @Transactional
    public void createAvatarFromUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        String[] parts = username.trim().split("\\s+");
        String initials;
        if (parts.length >= 2) {
            initials = (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        } else if (username.length() >= 2) {
            initials = username.substring(0, 2).toUpperCase();
        } else {
            initials = username.toUpperCase();
        }
        user.setAvatarInitials(initials);
        user.setAvatarBackgroundColor(generateRandomHexColor());
        userRepository.save(user);
    }

    private String generateRandomHexColor() {
        Random random = new Random();
        int r = random.nextInt(256);
        int g = random.nextInt(256);
        int b = random.nextInt(256);
        return String.format("#%02X%02X%02X", r, g, b);
    }
}
