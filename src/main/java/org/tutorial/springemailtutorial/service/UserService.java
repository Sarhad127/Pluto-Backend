package org.tutorial.springemailtutorial.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.tutorial.springemailtutorial.model.User;
import org.tutorial.springemailtutorial.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
}
