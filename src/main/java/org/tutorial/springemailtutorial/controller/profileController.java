package org.tutorial.springemailtutorial.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.tutorial.springemailtutorial.dto.PasswordChangeRequest;
import org.tutorial.springemailtutorial.model.User;
import org.tutorial.springemailtutorial.repository.UserRepository;
import org.tutorial.springemailtutorial.service.JwtService;
import org.tutorial.springemailtutorial.service.UserService;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class profileController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    private static final Logger logger = LoggerFactory.getLogger(profileController.class);

    public profileController(UserService userService, UserRepository userRepository, JwtService jwtService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @PutMapping("/update-username")
    public ResponseEntity<?> updateUsername(@RequestBody Map<String, String> request, Authentication authentication) {
        String newUsername = request.get("username");
        String currentUsername = authentication.getName();

        logger.info("Received request to update username for user: {}", currentUsername);
        logger.debug("New username: {}", newUsername);

        try {
            userService.updateUsername(currentUsername, newUsername);
            User updatedUser = userRepository.findByUsername(newUsername)
                    .orElseThrow(() -> new RuntimeException("Updated user not found"));
            String newToken = jwtService.generateToken(updatedUser);
            logger.info("Username successfully updated for user: {}", currentUsername);
            return ResponseEntity.ok().body(Map.of(
                    "message", "Username updated successfully.",
                    "token", newToken
            ));
        } catch (Exception e) {
            logger.error("Error updating username for user: {}", currentUsername, e);
            return ResponseEntity.status(500).body(Map.of("message", "Failed to update username."));
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody PasswordChangeRequest request, Authentication authentication) {
        String username = authentication.getName();
        userService.changePassword(username, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok().body(Map.of("message", "Password updated successfully."));
    }

    @PostMapping("/verify-password")
    public ResponseEntity<?> verifyPassword(@RequestBody Map<String, String> request,
                                            Authentication authentication) {
        String username = authentication.getName();
        String password = request.get("password");

        try {
            boolean isValid = userService.verifyPassword(username, password);
            return ResponseEntity.ok().body(Map.of("isValid", isValid));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", "Password verification failed"));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(@RequestHeader("Authorization") String authHeader,
                                        @RequestBody Map<String, String> request) {
        String password = request.get("password");

        try {
            userService.deleteUser(authHeader, password);
            return ResponseEntity.ok().body(Map.of("message", "User deleted successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Failed to delete user."));
        }
    }
}
